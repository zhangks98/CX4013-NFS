from enum import Enum, unique
from typing import Any, List

from nfs.common.serialize import BUF_SIZE, ByteBuffer
from nfs.common.values import Value


@unique
class ResponseStatus(Enum):
    OK = 0
    BAD_REQUEST = 1
    NOT_FOUND = 2
    INTERNAL_ERROR = 3
    UNKNOWN = 4


class Response():
    def __init__(self, req_id: int, status: ResponseStatus, values: List[Value] = []):
        self.req_id: int = req_id
        self.status: ResponseStatus = status
        self.values: List[Value] = values

    def get_req_id(self) -> int:
        return self.req_id

    def get_status(self) -> int:
        return self.status

    def get_values(self) -> List[Value]:
        return self.values

    def get_py_values(self) -> List[Any]:
        return [v.get_val() for v in self.values]

    def to_bytes(self) -> bytes:
        num_values = len(self.values)
        payload = ByteBuffer.allocate(BUF_SIZE)
        payload.put_int(self.req_id)\
            .put_int(self.status.value)\
            .put_int(num_values)
        for val in self.values:
            if not isinstance(val, Value):
                raise TypeError('Illegal value type for marshalling.')
            payload.put_bytes(val.to_bytes())
        return payload.to_bytes()

    @classmethod
    def from_bytes(cls, data: bytes) -> 'Response':
        buf = ByteBuffer.wrap(data)
        req_id = buf.get_int()
        status = buf.get_int()
        if status < 0 or status >= 5:
            status = ResponseStatus.UNKNOWN
        else:
            status = ResponseStatus(status)
        num_values = buf.get_int()
        values = []
        for _ in range(num_values):
            values.append(Value.from_bytes(buf))
        return cls(req_id, status, values)
