package sg.edu.ntu.nfs.common;

import java.io.InvalidClassException;

public interface Serializer {
    int BUF_SIZE = 4096;

    byte[] toBytes() throws InvalidClassException;
}
