package sg.edu.ntu.nfs.common.values;

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

    public long getVal() {
        return val;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + size());
        payload.putInt(ValueType.INT64.ordinal())
                .putLong(val);
        return payload.array();
    }
}

