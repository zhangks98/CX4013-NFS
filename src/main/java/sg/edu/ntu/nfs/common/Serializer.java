package sg.edu.ntu.nfs.common;

import java.io.InvalidClassException;

public interface Serializer {
    int BUF_SIZE = 1024;

    byte[] toBytes() throws InvalidClassException;
}
