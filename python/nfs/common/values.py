from enum import Enum, unique
from struct import pack

from nfs.common.serialize import ByteBuffer


class Value:
    def __init__(self, val):
        self.val = val

    def get_val(self):
        return self.val

    def to_bytes(self) -> bytes:
        raise NotImplementedError

    @staticmethod
    def from_bytes(buffer: ByteBuffer) -> 'Value':
        value_type = buffer.get()
        if value_type < 0 or value_type >= 4:
            raise ValueError("No such value type: {}".format(value_type))
        return ValueType(value_type).val_cls.from_bytes(buffer)


class Str(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Str':
        size = buffer.get_int()
        return cls(buffer.get_bytes(size).decode('utf-8'))

    def to_bytes(self) -> bytes:
        encoded = self.val.encode('utf-8')
        size = len(encoded)
        return pack('>bi{}s'.format(len(self.val)),
                    ValueType.STRING.value, size, encoded)


class Bytes(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Bytes':
        size = buffer.get_int()
        return cls(buffer.get_bytes(size))

    def to_bytes(self) -> bytes:
        size = len(self.val)
        return pack('>bi{}s'.format(size),
                    ValueType.BYTES.value, size, self.val)


class Int32(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Int32':
        return cls(buffer.get_int())

    def to_bytes(self) -> bytes:
        return pack('>bi', ValueType.INT32.value, self.val)


class Int64(Value):
    @classmethod
    def from_bytes(cls, buffer: ByteBuffer) -> 'Int64':
        return cls(buffer.get_long())

    def to_bytes(self) -> bytes:
        return pack('>bq', ValueType.INT64.value, self.val)


@unique
class ValueType(bytes, Enum):
    def __new__(cls, value, val_cls: Value):
        obj = bytes.__new__(cls, [value])
        obj._value_ = value
        obj.val_cls = val_cls
        return obj
    STRING = (0, Str)
    BYTES = (1, Bytes)
    INT32 = (2, Int32)
    INT64 = (3, Int64)
