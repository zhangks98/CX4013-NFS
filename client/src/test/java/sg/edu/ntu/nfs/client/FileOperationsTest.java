package sg.edu.ntu.nfs.client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class FileOperationsTest {
    private Proxy stub;
    private CacheHandler cacheHandler;
    private FileOperations fileOps;

    @Before
    public void setUp() {
        stub = mock(Proxy.class);
        cacheHandler = new CacheHandler(stub, 1000);
        fileOps = new FileOperations(cacheHandler);
    }


    @Test
    public void get_file() {
        String path = "file.txt";
        byte[] expected = new byte[] {0xa, 0xb, 0xc};
        when(stub.requestFile(path)).thenReturn(expected);
        byte[] actual = fileOps.read(path, 0, expected.length);
        assertArrayEquals(expected, actual);
        actual = fileOps.read(path, 1, expected.length + 1);
        assertArrayEquals(Arrays.copyOfRange(expected, 1, expected.length), actual);
        actual = fileOps.read(path, expected.length, 1);
        assertArrayEquals(null, actual);
    }
}
