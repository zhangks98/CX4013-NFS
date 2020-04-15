package nfs.common.values;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;

public class Int32 implements Value {
    private final int val;

    public Int32(int val) {
        this.val = val;
    }

    public Int32(ByteBuffer valBuffer) {
        this.val = valBuffer.getInt();
    }

    @Override
    public int size() {
        return Integer.BYTES;
    }

    @Override
    public ValueType getType() {
        return ValueType.INT32;
    }

    @Override
    public Integer getVal() {
        return val;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        ByteBuffer payload = ByteBuffer.allocate(1 + size());
        payload.put((byte) ValueType.INT32.ordinal())
                .putInt(val);
        return payload.array();
    }
}
