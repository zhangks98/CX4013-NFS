package nfs.common.values;

import nfs.common.Serializer;

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
}
