from enum import Enum, unique
from struct import pack

from nfs.common.serialize import ByteBuffer


@unique
class ValueType(Enum):
    STRING = 0
    BYTES = 1
    INT32 = 2
    INT64 = 3


class Value:
    def __init__(self, val):
        self.val = val

    def get_val(self):
        return self.val

    def to_bytes(self) -> bytes:
        raise NotImplementedError

    @staticmethod
    def from_bytes(buffer: ByteBuffer) -> 'Value':
        value_type = buffer.get_int()
        if value_type == ValueType.STRING.value:
            return Str.from_bytes(buffer)
        if value_type == ValueType.BYTES.value:
            return Bytes.from_bytes(buffer)
        if value_type == ValueType.INT32.value:
            return Int32.from_bytes(buffer)
        if value_type == ValueType.INT64.value:
            return Int64.from_bytes(buffer)
        raise ValueError("No such value type: {}".format(value_type))


class Str(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Str':
        size = buffer.get_int()
        return cls(buffer.get_bytes(size).decode('utf-8'))

    def to_bytes(self) -> bytes:
        encoded = self.val.encode('utf-8')
        size = len(encoded)
        return pack('>ii{}s'.format(len(self.val)),
                    ValueType.STRING.value, size, encoded)


class Bytes(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Bytes':
        size = buffer.get_int()
        return cls(buffer.get_bytes(size))

    def to_bytes(self) -> bytes:
        size = len(self.val)
        return pack('>ii{}s'.format(size),
                    ValueType.BYTES.value, size, self.val)


class Int32(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Int32':
        return cls(buffer.get_int())

    def to_bytes(self) -> bytes:
        return pack('>ii', ValueType.INT32.value, self.val)


class Int64(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Int64':
        return cls(buffer.get_long())

    def to_bytes(self) -> bytes:
        return pack('>iq', ValueType.INT64.value, self.val)
