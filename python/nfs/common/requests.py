from enum import Enum, unique
from typing import List

from nfs.common.serialize import BUF_SIZE, ByteBuffer
from nfs.common.values import Value


@unique
class RequestName(Enum):
    EMPTY = (0, 0)
    READ = (1, 3)
    WRITE = (2, 4)
    GET_ATTR = (3, 1)
    LIST_DIR = (4, 1)
    TOUCH = (5, 1)
    REGISTER = (6, 2)
    FILE_UPDATED = (7, 2)

    def __init__(self, ordinal: int, num_params: int):
        self.ordinal: int = ordinal
        self.num_params: int = num_params


class Request():
    def __init__(self, id: int, name: RequestName):
        self.req_id: int = id
        self.name: RequestName = name
        self.params: List[Value] = []

    def get_id(self) -> int:
        return self.req_id

    def get_name(self) -> RequestName:
        return self.name

    def add_param(self, val):
        if len(self.params) >= self.name.num_params:
            raise ValueError(
                'Parameter length exceeds {}'.format(self.name.num_params))
        self.params.append(val)

    def get_param(self, pos) -> Value:
        return self.params[pos]

    def set_param(self, pos, val):
        self.params[pos] = val

    def to_bytes(self) -> bytes:
        if len(self.params) != self.name.num_params:
            raise ValueError("Unable to serialize request {}: wrong number of parameters. Expected: {}, Actual: {}.".format(
                self.name, self.name.num_params, len(self.params)))
        payload = ByteBuffer.allocate(BUF_SIZE)
        payload.put_int(self.req_id)\
            .put_int(self.name.ordinal)\
            .put_int(self.name.num_params)
        for val in self.params:
            payload.put_bytes(val.to_bytes())
        return payload.to_bytes()


class ReadRequest(Request):
    pass


class WriteRequest(Request):
    pass


class GetAttrRequest(Request):
    pass


class ListDirRequest(Request):
    pass


class TouchRequest(Request):
    pass


class RegisterRequest(Request):
    pass


class FileUpdatedRequest(Request):
    pass
