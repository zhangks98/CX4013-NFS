import logging
import os
import socket
import time
from pathlib import Path
from typing import List, Optional

from nfs.common.exceptions import BadRequestError, NotFoundError
from nfs.common.requests import (AppendRequest, EmptyRequest,
                                 FileUpdatedCallback, GetAttrRequest,
                                 InsertRequest, ListDirRequest, ReadRequest,
                                 RegisterRequest, Request, RequestName,
                                 TouchRequest)
from nfs.common.values import Bytes, Int64, Str, Value

logger = logging.getLogger(__name__)


class ALOServicer:
    def __init__(self, root_dir: str, sock: socket.socket):
        self.root_dir = root_dir
        self.sock = sock
        # file_path --> Dict<client_addr, { time_of_register, monitor_interval }>
        self.file_subscribers = {}

    def get_current_timestamp_millisecond(self):
        return int(time.time() * 1000)

    def update_file_subscribers(self, file_path: str, client_addr: str, time_of_register: int, monitor_interval: int):
        # Register the client with the path
        if file_path not in self.file_subscribers:
            self.file_subscribers[file_path] = {}
        # If already registered, update register time and monitor interval
        if client_addr in self.file_subscribers[file_path]:
            self.file_subscribers[file_path][client_addr] = {
                "time_of_register": time_of_register,  # Current timestamp
                "monitor_interval": monitor_interval
            }
            return []
        # If not present, register it
        self.file_subscribers[file_path][client_addr] = {
            "time_of_register": time_of_register,  # Current timestamp
            "monitor_interval": monitor_interval
        }

    def validate_file_path(self, path: str, combined_path: str):
        """
        Verify that the file exists and is not a directory.
        """
        logger.debug("path: {}".format(path))
        logger.debug("Combined path: {}".format(combined_path))
        if not os.path.exists(combined_path):
            raise NotFoundError(
                'File {} does not exist on the server'.format(path))
        if os.path.isdir(combined_path):
            raise BadRequestError('{} is a directory'.format(path))

    def send_update(self, path_to_file: str, mtime: int, data: bytes):
        if path_to_file not in self.file_subscribers:
            # No client subscribed to this file
            return
        subscriber_map: dict = self.file_subscribers[path_to_file]
        for client_addr in subscriber_map.copy():
            registry_info: dict = subscriber_map[client_addr]
            if self.get_current_timestamp_millisecond() > registry_info["time_of_register"] + registry_info[
                    "monitor_interval"]:
                # Expired, remove it
                del subscriber_map[client_addr]
            else:
                # Send update
                callback_req = FileUpdatedCallback(
                    path=path_to_file, mtime=mtime, data=data)
                try:
                    self.sock.sendto(callback_req.to_bytes(), client_addr)
                except OSError as e:
                    logger.warning(
                        "Error sending callback to %s: %s", client_addr, e)

    def handle(self, req, addr) -> List[Value]:
        req_name = req.get_name()
        if req_name == RequestName.EMPTY:
            return self.handle_empty(req)
        if req_name == RequestName.READ:
            return self.handle_read(req)
        if req_name == RequestName.INSERT:
            return self.handle_insert(req)
        if req_name == RequestName.GET_ATTR:
            return self.handle_get_attr(req)
        if req_name == RequestName.LIST_DIR:
            return self.handle_list_dir(req)
        if req_name == RequestName.TOUCH:
            return self.handle_touch(req)
        if req_name == RequestName.REGISTER:
            return self.handle_register(req, addr)
        if req_name == RequestName.APPEND:
            return self.handle_append(req)
        raise BadRequestError('Request name not found.')

    def handle_empty(self, req: EmptyRequest):
        return []

    def handle_read(self, req: ReadRequest):
        path = req.get_path()
        combined_path = os.path.join(self.root_dir, path)
        self.validate_file_path(path, combined_path)
        with open(combined_path, 'rb') as f:
            content = f.read()
        return [Bytes(content)]

    def handle_insert(self, req: InsertRequest):
        path = req.get_path()
        offset = req.get_offset()
        data = req.get_data()
        logger.debug(
            "Arguments - path: {}, offset: {}, data: {}".format(path, offset, data))
        if offset < 0:
            raise BadRequestError("offset < 0")
        combined_path = os.path.join(self.root_dir, path)
        self.validate_file_path(path, combined_path)
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
            f.seek(0)
            file_content = f.read()
        # Update subscribers
        mtime = int(os.path.getmtime(combined_path) * 1000)
        self.send_update(path_to_file=path,
                         mtime=mtime, data=file_content)
        # Returns an acknowledgement to the client upon successful write
        return []

    def handle_append(self, req: AppendRequest):
        path = req.get_path()
        data = req.get_data()
        logger.debug(
            "Arguments - path: {}, data: {}".format(path, data))
        combined_path = os.path.join(self.root_dir, path)
        self.validate_file_path(path, combined_path)
        with open(combined_path, "ab+") as f:
            f.write(data)  # Append the data
            f.seek(0)
            file_content = f.read()
        # Update subscribers
        mtime = int(os.path.getmtime(combined_path) * 1000)
        self.send_update(path_to_file=path,
                         mtime=mtime, data=file_content)
        return []

    def handle_get_attr(self, req: GetAttrRequest):
        path = req.get_path()
        logger.debug("Arguments - path: {}".format(path))
        combined_path = os.path.join(self.root_dir, path)
        logger.debug("Combined path: {}".format(combined_path))
        self.validate_file_path(path, combined_path)
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
        Path(combined_path).touch()
        atime = int(os.path.getatime(combined_path) * 1000)
        # Returns access time (timestamp) to the client upon successful touch
        return [Int64(atime)]

    def handle_register(self, req: RegisterRequest, client_addr: str):
        monitor_interval = req.get_monitor_interval()
        if monitor_interval < 0:
            raise BadRequestError("monitor_interval < 0")
        path = req.get_path()
        combined_path = os.path.join(self.root_dir, path)
        self.validate_file_path(path, combined_path)
        current_timestamp = self.get_current_timestamp_millisecond()
        self.update_file_subscribers(file_path=path, client_addr=client_addr,
                                     time_of_register=current_timestamp, monitor_interval=monitor_interval)
        return []


class AMOServicer(ALOServicer):
    def __init__(self, root_dir, sock):
        super().__init__(root_dir, sock)
        self.history_map = {}

    def create_identifier(self, req: Request, addr: any) -> str:
        return str(req.get_id()) + ":" + str(addr)

    def _is_duplicate_request(self, req: Request, addr: any) -> Optional[List[Value]]:
        identifier = self.create_identifier(req, addr)
        if identifier not in self.history_map:
            return None
        return self.history_map[identifier]

    def handle(self, req, addr) -> List[Value]:
        stored_res = self._is_duplicate_request(req, addr)
        if stored_res is not None:
            return stored_res
        # Call handle from parent class
        res = super().handle(req, addr)
        identifier = self.create_identifier(req, addr)
        # Save it to history
        self.history_map[identifier] = res
        return res
