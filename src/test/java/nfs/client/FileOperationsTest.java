package nfs.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void readValidOffsetValidCount() {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);

        try {
            when(stub.requestFile(path)).thenReturn(opt_expected);
            Optional<byte[]> actual = fileOps.read(path, 0, expected.length);

            assertTrue(actual.isPresent());
            assertArrayEquals(expected, actual.get());

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Test
    public void readValidOffsetInvalidCount() {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);

        try {
            when(stub.requestFile(path)).thenReturn(opt_expected);
            Optional<byte[]> actual = fileOps.read(path, 1, expected.length + 1);

            assertTrue(actual.isPresent());
            assertArrayEquals(Arrays.copyOfRange(expected, 1, expected.length), actual.get());

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    @Test
    public void readInvalidOffsetValidCount() {
        String path = "file.txt";
        byte[] expected = new byte[]{0xa, 0xb, 0xc};
        Optional<byte[]> opt_expected = Optional.of(expected);

        try {
            when(stub.requestFile(path)).thenReturn(opt_expected);
            Optional<byte[]> actual = fileOps.read(path, expected.length, 1);

            assertFalse(actual.isPresent());

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
