from enum import Enum, unique
from typing import List

from nfs.common.serialize import BUF_SIZE, ByteBuffer
from nfs.common.values import Bytes, Int64, Str, Value


class Request():
    def __init__(self, id: int, name: 'RequestName'):
        """
        Server only constructs request from the parser,
        except for FileUpdatedCallback
        """
        self.req_id: int = id
        self.name: 'RequestName' = name
        self.__params: List[Value] = []

    def get_id(self) -> int:
        return self.req_id

    def get_name(self) -> 'RequestName':
        return self.name

    def add_param(self, val):
        if len(self.__params) >= self.name.num_params:
            raise ValueError(
                'Parameter length exceeds {}'.format(self.name.num_params))
        self.__params.append(val)

    def get_param(self, pos) -> Value:
        return self.__params[pos]

    def set_param(self, pos, val):
        self.__params[pos] = val

    def to_bytes(self) -> bytes:
        if len(self.__params) != self.name.num_params:
            raise ValueError('Unable to marshal request {}: wrong number of parameters. Expected: {}, Actual: {}.'.format(
                self.name.name, self.name.num_params, len(self.__params)))
        payload = ByteBuffer.allocate(BUF_SIZE)
        payload.put_request_header(
            self.req_id, self.name.value, self.name.num_params)
        for val in self.__params:
            if not isinstance(val, Value):
                raise TypeError('Illegal value type for marshalling.')
            payload.put_bytes(val.to_bytes())
        return payload.to_bytes()

    @staticmethod
    def from_bytes(data: bytes) -> 'Request':
        buf = ByteBuffer.wrap(data)
        req_id, req_name_ind, num_params = buf.get_request_header()
        if req_name_ind < 0 or req_name_ind >= len(RequestName):
            raise ValueError("Unable to parse request: unknown request name")
        if req_name_ind == 8:
            raise NotImplementedError(
                "Server does not handle FileUpdatedCallback.")
        req = RequestName(req_name_ind).req_cls(req_id)

        if num_params != req.get_name().num_params:
            raise ValueError('Unable to parse request {}: wrong number of parameters. Expected: {}, Actual: {}.'.format(
                req.get_name().name, req.get_name().num_params, num_params))

        for _ in range(num_params):
            req.add_param(Value.from_bytes(buf))
        return req


class EmptyRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.EMPTY)


class ReadRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.READ)

    def get_path(self) -> str:
        return self.get_param(0).get_val()


class InsertRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.INSERT)

    def get_path(self) -> str:
        return self.get_param(1).get_val()

    def get_offset(self) -> int:
        return self.get_param(0).get_val()

    def get_data(self) -> bytes:
        return self.get_param(2).get_val()


class AppendRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.APPEND)

    def get_path(self) -> str:
        return self.get_param(0).get_val()

    def get_data(self) -> bytes:
        return self.get_param(1).get_val()


class GetAttrRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.GET_ATTR)

    def get_path(self) -> str:
        return self.get_param(0).get_val()


class ListDirRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.LIST_DIR)

    def get_path(self) -> str:
        return self.get_param(0).get_val()


class TouchRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.TOUCH)

    def get_path(self) -> str:
        return self.get_param(0).get_val()


class RegisterRequest(Request):
    def __init__(self, id: int):
        super().__init__(id, RequestName.REGISTER)

    def get_monitor_interval(self) -> int:
        return self.get_param(0).get_val()

    def get_path(self) -> str:
        return self.get_param(1).get_val()


class FileUpdatedCallback(Request):
    def __init__(self, path: str, mtime: int, data: bytes):
        """
        Callbacks uses static id 0, since no response is needed.
        """
        super().__init__(id=0, name=RequestName.FILE_UPDATED)
        self.add_param(Str(path))
        self.add_param(Int64(mtime))
        self.add_param(Bytes(data))

    def get_path(self) -> str:
        return self.get_param(0).get_val()

    def get_mtime(self) -> int:
        return self.get_param(1).get_val()

    def get_data(self) -> bytes:
        return self.get_param(2).get_val()


@unique
class RequestName(bytes, Enum):
    def __new__(cls, value, num_params, req_cls):
        """
        The constructor for a RequestName Enum.

        Args:
            value: The numeric tag for the request name.
            num_params: The number of parameters for the request.
            req_cls: The constructor for the request, used for deserializing.
        """
        obj = bytes.__new__(cls, [value])
        obj._value_ = value
        obj.num_params = num_params
        obj.req_cls = req_cls
        return obj

    EMPTY = (0, 0, EmptyRequest)
    READ = (1, 1, ReadRequest)
    INSERT = (2, 3, InsertRequest)
    GET_ATTR = (3, 1, GetAttrRequest)
    LIST_DIR = (4, 1, ListDirRequest)
    TOUCH = (5, 1, TouchRequest)
    REGISTER = (6, 2, RegisterRequest)
    APPEND = (7, 2, AppendRequest)

    # FileUpdatedCallback does not construct from parser.
    FILE_UPDATED = (8, 3, None)
