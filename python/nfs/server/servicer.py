import logging
import os
import socket
import time
from pathlib import Path
from typing import List, Optional

from nfs.common.exceptions import BadRequestError, NotFoundError
from nfs.common.requests import (EmptyRequest, FileUpdatedCallback,
                                 GetAttrRequest, ListDirRequest, ReadRequest,
                                 RegisterRequest, Request, RequestName,
                                 TouchRequest, WriteRequest)
from nfs.common.responses import Response, ResponseStatus
from nfs.common.values import Bytes, Int32, Int64, Str, Value

logger = logging.getLogger(__name__)


class ALOServicer:
    def __init__(self, root_dir: str, sock: socket.socket):
        self.root_dir = root_dir
        self.sock = sock
        self.file_subscriber = {}  # path --> List<Tuple(client_identifier, time_of_register, monitor_interval)>

    def handle(self, req, addr) -> List[Value]:
        req_name = req.get_name()
        if req_name == RequestName.EMPTY:
            return self.handle_empty(req)
        if req_name == RequestName.READ:
            return self.handle_read(req)
        if req_name == RequestName.WRITE:
            return self.handle_write(req)
        if req_name == RequestName.GET_ATTR:
            return self.handle_get_attr(req)
        if req_name == RequestName.LIST_DIR:
            return self.handle_list_dir(req)
        if req_name == RequestName.TOUCH:
            return self.handle_touch(req)
        if req_name == RequestName.REGISTER:
            return self.handle_register(req, addr)
        raise BadRequestError('Request name not found.')

    def handle_empty(self, req: EmptyRequest):
        return []

    def handle_read(self, req: ReadRequest):
        path = req.get_path()
        logger.debug("path: {}".format(path))
        combined_path = os.path.join(self.root_dir, path)
        logger.debug("Combined path: {}".format(combined_path))
        # Check whether file_path exists
        if not os.path.exists(combined_path):
            raise NotFoundError(
                'File {} does not exist on the server'.format(path))
        if os.path.isdir(combined_path):
            raise BadRequestError('{} is a directory'.format(path))
        with open(combined_path, 'rb') as f:
            content = f.read()
        return [Bytes(content)]

    def handle_write(self, req: WriteRequest):
        path = req.get_path()
        offset = req.get_offset()
        data = req.get_data()
        logger.debug(
            "Arguments - path: {}, offset: {}, data: {}".format(path, offset, data))
        combined_path = os.path.join(self.root_dir, path)
        logger.debug("Combined path: {}".format(combined_path))
        # If file does not exist on the server, returns error
        if not os.path.exists(combined_path):
            raise NotFoundError(
                'File {} does not exist on the server'.format(path))
        if os.path.isdir(combined_path):
            raise BadRequestError('{} is a directory'.format(path))
        # If offset exceeds the file length, returns error
        file_size = os.path.getsize(combined_path)
        if offset > file_size:
            raise BadRequestError(
                "Offset {} exceeds the file length {}".format(offset, file_size))
        with open(combined_path, "ab+") as f:
            f.seek(offset)
            remaining_content = f.read()  # Save the content after offset
            f.seek(offset)
            f.truncate()  # Remove the content after offset
            f.write(data)  # Append the data
            f.write(remaining_content)  # Append the remaining content
        # Returns an acknowledgement to the client upon successful write
        return []

    def handle_get_attr(self, req: GetAttrRequest):
        path = req.get_path()
        logger.debug("Arguments - path: {}".format(path))
        combined_path = os.path.join(self.root_dir, path)
        logger.debug("Combined path: {}".format(combined_path))
        if not os.path.exists(combined_path):
            raise NotFoundError(
                'File {} does not exist on the server'.format(path))
        atime = int(os.path.getatime(combined_path) * 1000)
        mtime = int(os.path.getmtime(combined_path) * 1000)
        return [Int64(mtime), Int64(atime)]

    def handle_list_dir(self, req: ListDirRequest):
        path = req.get_path()
        combined_path = os.path.join(self.root_dir, path)
        if not os.path.isdir(path):
            raise NotFoundError('{} is not a directory'.format(path))
        with os.scandir(combined_path) as it:
            files = [Str(entry.name + '/') if entry.is_dir()
                     else Str(entry.name) for entry in it]
        return files

    def handle_touch(self, req: TouchRequest):
        path = req.get_path()
        logger.debug("Arguments - path: {}".format(path))
        combined_path = os.path.join(self.root_dir, path)
        logger.debug("Combined path: {}".format(combined_path))
        Path(path).touch()
        atime = int(os.path.getatime(combined_path) * 1000)
        # Returns access time (timestamp) to the client upon successful touch
        return [Int64(atime)]

    def handle_register(self, req: RegisterRequest, addr):
        monitor_interval = req.get_monitor_interval()
        path = req.get_path()
        combined_path = os.path.join(self.root_dir, path)
        # Check whether file exists
        if not os.path.exists(combined_path):
            raise NotFoundError(
                'File {} does not exist on the server'.format(path))
        # Check whether it is a file
        if os.path.isdir(combined_path):
            raise BadRequestError('{} is a directory'.format(path))
        # Register the client with the path
        if combined_path not in self.file_subscriber:
            self.file_subscriber[combined_path] = []
        client_addr, client_port = self.sock.getsockname()
        client_identifier = client_addr + ":" + client_port
        # If already registered, update register time and monitor interval
        for i in range(len(self.file_subscriber[combined_path])):
            entry = self.file_subscriber[combined_path][i]
            if entry["id"] == client_identifier:
                entry["time_of_register"] = int(time.time() * 1000)  # Current timestamp
                entry["monitor_interval"] = int(monitor_interval)
                return []
        # If not present, register it
        self.file_subscriber[combined_path].append({
            "id": client_identifier,
            "time_of_register": int(time.time() * 1000),  # Current timestamp
            "monitor_interval": int(monitor_interval)
        })
        return []


class AMOServicer(ALOServicer):
    def __init__(self, root_dir, sock):
        super().__init__(root_dir, sock)
        self.historyMap = {}

    # TODO(ming): use explicit type for addr
    def _create_identifier(self, req: Request, addr: any) -> str:
        return str(req.get_id()) + ":" + str(addr)

    # TODO(ming): use explicit type for addr
    def _is_duplicate_request(self, req: Request, addr: any) -> Optional[List[Value]]:
        identifier = self._create_identifier(req, addr)
        if identifier not in self.historyMap:
            return None
        return self.historyMap[identifier]

    def handle(self, req, addr) -> List[Value]:
        stored_res = self._is_duplicate_request(req, addr)
        if stored_res is not None:
            return stored_res
        # Call handle from parent class
        res = super().handle(req, addr)
        identifier = self._create_identifier(req, addr)
        # Save it to history
        self.historyMap[identifier] = res
        return res
