package sg.edu.ntu.nfs.common.values;

import sg.edu.ntu.nfs.common.Serializer;

import java.nio.ByteBuffer;

public abstract class Value implements Serializer {
    public static Value parseFrom(ByteBuffer data) {
        return null;
    }

    public abstract int size();
}
