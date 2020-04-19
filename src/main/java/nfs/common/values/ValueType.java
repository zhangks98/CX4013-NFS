package nfs.common.values;

import java.nio.ByteBuffer;

public enum ValueType {
    STRING(Str::new),
    BYTES(Bytes::new),
    INT32(Int32::new),
    INT64(Int64::new);

    private final ValueConstructor cls;

    ValueType(ValueConstructor cls) {
        this.cls = cls;
    }

    public Value build(ByteBuffer buf) {
        return cls.get(buf);
    }
}

/**
 * A functional interface to the constructor of a request type.
 */
interface ValueConstructor {
    /**
     * Construct a value with ByteBuffer.
     *
     * @param buf the buffer that points to the value.
     * @return the constructed value.
     */
    Value get(ByteBuffer buf);
}
