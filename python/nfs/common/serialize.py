from struct import pack_into, unpack_from

BUF_SIZE = 4096
INTEGER_BYTES = 4
LONG_BYTES = 8


class ByteBuffer():

    def __init__(self, buf: bytearray):
        self.offset = 0
        self.buf = buf

    @classmethod
    def allocate(cls, size: int):
        return cls(bytearray(size))

    @classmethod
    def wrap(cls, array: bytes):
        return cls(bytearray(array))

    def to_bytes(self) -> bytes:
        return bytes(self.buf)

    def put_int(self, v: int):
        pack_into('>i', self.buf, self.offset, v)
        self.offset += INTEGER_BYTES
        return self

    def put_long(self, v: int):
        pack_into('>q', self.buf, self.offset, v)
        self.offset += LONG_BYTES
        return self

    def put_bytes(self, v: bytes):
        pack_into('>{}s'.format(len(v)), self.buf, self.offset, v)
        self.offset += len(v)
        return self

    def get_int(self) -> int:
        v = unpack_from('>i', self.buf, self.offset)[0]
        self.offset += INTEGER_BYTES
        return v

    def get_long(self) -> int:
        v = unpack_from('>q', self.buf, self.offset)[0]
        self.offset += LONG_BYTES
        return v

    def get_bytes(self, size: int) -> bytes:
        v = unpack_from('>{}s'.format(size), self.buf, self.offset)[0]
        self.offset += size
        return v
