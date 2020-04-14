package nfs.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

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
        Optional<byte[]> opt_expected = Optional.of(expected);

        try {
            when(stub.requestFile(path)).thenReturn(opt_expected);
            Optional<byte[]> actual = cacheHandler.getFile(path);
            byte[] cached = cacheHandler.getCache().getFile(path).getFileContent();

            assertTrue(actual.isPresent());
            assertArrayEquals(expected, actual.get());
            assertArrayEquals(expected, cached);

        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
