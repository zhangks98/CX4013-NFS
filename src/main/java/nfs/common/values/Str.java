package nfs.common.values;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Str implements Value {
    private final String val;
    private final byte[] encodedVal;

    public Str(String val) {
        this.val = val;
        this.encodedVal = val.getBytes(StandardCharsets.UTF_8);
    }

    public Str(ByteBuffer valBuffer) {
        int length = valBuffer.getInt();
        this.encodedVal = new byte[length];
        valBuffer.get(encodedVal, 0, length);
        this.val = new String(encodedVal, StandardCharsets.UTF_8);
    }


    public String getVal() {
        return val;
    }

    @Override
    public int size() {
        return Integer.BYTES + encodedVal.length;
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + size());
        payload.putInt(ValueType.STRING.ordinal())
                .putInt(encodedVal.length)
                .put(encodedVal);
        return payload.array();
    }
}
