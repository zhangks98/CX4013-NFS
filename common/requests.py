from enum import Enum, unique
from serialize import ByteBuffer, BUF_SIZE
from values import Value
from typing import List
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
        self.ordinal = ordinal
        self.num_params = num_params

class Request():
    def __init__(self, id: int, name: RequestName):
        self.id = id
        self.name = name
        self.params: List[Value] = []
    
    def get_id(self) -> int:
        return self.id

    def get_name(self) -> RequestName:
        return self.name
    
    def add_param(self, val):
        if (len(self.params) >= self.name.num_params):
            raise ValueError('Parameter length exceeds {}'.format(self.name.num_params))
        self.params.append(val)
    
    def get_param(self, pos) -> Value:
        return self.params[pos]
    
    def set_param(self, pos, val):
        self.params[pos] = val
    
    def to_bytes(self) -> bytes:
        if (len(self.params) != self.name.num_params):
            raise ValueError("Unable to serialize request {}: wrong number of parameters. Expected: {}, Actual: {}.".format(self.name, self.name.num_params, len(self.params)))
        payload = ByteBuffer.allocate(BUF_SIZE)
        payload.put_int(self.id)\
            .put_int(self.name.ordinal)\
                .put_int(self.name.num_params)
        for val in self.params:
            payload.put_bytes(val.to_bytes())
        return payload.to_bytes()