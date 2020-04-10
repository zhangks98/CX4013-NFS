import logging
import os
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
    def __init__(self, root_dir, sock):
        self.root_dir = root_dir
        self.sock = sock

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
        raise NotImplementedError

    def handle_write(self, req: WriteRequest):
        raise NotImplementedError

    def handle_get_attr(self, req: GetAttrRequest):
        raise NotImplementedError

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
        raise NotImplementedError

    def handle_register(self, req: RegisterRequest, addr):
        raise NotImplementedError


class AMOServicer(ALOServicer):
    def __init__(self, root_dir, sock):
        super().__init__(root_dir, sock)
        self.historyMap = {}

    # TODO(ming): use explicit type for addr
    def _create_identifier(self, req: Request, addr: any) -> str:
        return str(req.get_id()) + ":" + str(addr);

    # TODO(ming): use explicit type for addr
    def _is_duplicate_request(self, req: Request, addr: any) -> Optional[Response]:
        identifier = self._create_identifier(req, addr);
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
