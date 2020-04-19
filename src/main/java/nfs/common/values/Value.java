package nfs.common.values;

import nfs.common.Serializer;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

public interface Value extends Serializer {
    /**
     * Get the byte size of the underlying value, excluding the 4-byte type information.
     *
     * @return byte size of the underlying value.
     */
    int size();

    /**
     * Get the type of the underlying value.
     *
     * @return value type.
     */
    ValueType getType();

    /**
     * Get the underlying value.
     *
     * @return value type.
     */
    Object getVal();

    /**
     * Put the value to a ByteBuffer
     */
    void putBytes(ByteBuffer buf);

    class Builder {
        private static final ValueType[] VALUE_TYPES = ValueType.values();

        public static Value parseFrom(ByteBuffer data) throws InvalidObjectException {
            int valueTypeIndex = data.get();
            if (valueTypeIndex < 0 || valueTypeIndex >= VALUE_TYPES.length)
                throw new InvalidObjectException("Unable to parse value: no matching value type.");
            ValueType type = VALUE_TYPES[valueTypeIndex];
            return type.build(data);
        }
    }
}
