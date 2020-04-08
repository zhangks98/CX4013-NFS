from enum import Enum, unique

# Python's Enum starts from 1 to prevent being interpreted as False.
# Therefore, EMPTY request is ignored.
@unique
class RequestName(Enum):
    READ = (1, 3)
    WRITE = (2, 4)
    GET_ATTR = (3, 1)
    LIST_DIR = (4, 1)
    TOUCH = (5, 1)
    REGISTER = (6, 2)
    FILE_UPDATED = (7, 2)

    def __init__(self, ordinal: int, numParams: int):
        self.ordinal = ordinal
        self.numParams = numParams

class Request():
    def __init__(self, id: int, name: RequestName):
        self.id = id
        self.name = name
        self.numParams = name.numParams
        self.params = []
        

