package sg.edu.ntu.nfs.client;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class CacheHandlerTest {
    private Proxy stub;
    private CacheHandler cacheHandler;

    @Before
    public void setUp() {
        stub = mock(Proxy.class);
        cacheHandler = new CacheHandler(stub, 1000);
    }

    @Test
    public void get_file() {
        String path = "abc";
        byte[] expected = new byte[] {0xa, 0xb, 0xc};
        when(stub.requestFile(path)).thenReturn(expected);
        byte[] actual = cacheHandler.getFile(path);
        byte[] cached = cacheHandler.getCache().getFile(path).getFileContent();
        assertArrayEquals(expected, actual);
        assertArrayEquals(expected, cached);
    }

}
