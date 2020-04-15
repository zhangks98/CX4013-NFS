package nfs.common.values;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;

public class Int64 implements Value {
    private final long val;

    public Int64(long val) {
        this.val = val;
    }

    public Int64(ByteBuffer valBuffer) {
        this.val = valBuffer.getLong();
    }

    @Override
    public int size() {
        return Long.BYTES;
    }

    @Override
    public ValueType getType() {
        return ValueType.INT64;
    }

    public Long getVal() {
        return val;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        ByteBuffer buf = ByteBuffer.allocate(1 + size());
        putBytes(buf);
        return buf.array();
    }

    @Override
    public void putBytes(ByteBuffer buf) {
        buf.put((byte) ValueType.INT64.ordinal())
                .putLong(val);
    }
}

