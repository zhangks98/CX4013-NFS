package nfs.common.values;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;

public class Bytes implements Value {
    private final byte[] val;

    public Bytes(byte[] val) {
        this.val = val;
    }

    public Bytes(ByteBuffer valBuffer) {
        int length = valBuffer.getInt();
        this.val = new byte[length];
        valBuffer.get(val, 0, length);
    }

    public byte[] getVal() {
        return val;
    }

    @Override
    public int size() {
        return Integer.BYTES + val.length;
    }

    @Override
    public ValueType getType() {
        return ValueType.BYTES;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        ByteBuffer buf = ByteBuffer.allocate(1 + size());
        putBytes(buf);
        return buf.array();
    }

    @Override
    public void putBytes(ByteBuffer buf) {
        buf.put((byte) ValueType.BYTES.ordinal())
                .putInt(val.length)
                .put(val);
    }

}
