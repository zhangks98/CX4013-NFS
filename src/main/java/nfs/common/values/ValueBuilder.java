package nfs.common.values;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

public class ValueBuilder {
    private static final ValueType[] VALUE_TYPES = ValueType.values();

    public static Value parseFrom(ByteBuffer data) throws InvalidObjectException {
        int valueTypeIndex = data.get();
        if (valueTypeIndex < 0 || valueTypeIndex >= VALUE_TYPES.length)
            throw new InvalidObjectException("Unable to parse value: value type index out of bound.");
        ValueType type = VALUE_TYPES[valueTypeIndex];
        switch (type) {
            case INT32:
                return new Int32(data);
            case INT64:
                return new Int64(data);
            case BYTES:
                return new Bytes(data);
            case STRING:
                return new Str(data);
            default:
                throw new InvalidObjectException("Unable to parse value: no matching value type");
        }
    }
}
