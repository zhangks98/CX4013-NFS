package nfs.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void readEmptyFile() throws IOException {
        String path = "file.txt";
        byte[] expected = new byte[]{};
        Optional<byte[]> opt_expected = Optional.of(expected);

        when(stub.requestFile(path)).thenReturn(opt_expected);
        Optional<byte[]> actual = fileOps.read(path, 0, expected.length);

        assertTrue(actual.isPresent());
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void readNegativeOffset() throws IOException {
        String path = "file.txt";

        Optional<byte[]> actual = fileOps.read(path, -1, 1);

        assertFalse(actual.isPresent());
        verify(stub, never()).requestFile(path);
    }

    @Test
    public void readOutOfRangeOffset() throws IOException {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);

        when(stub.requestFile(path)).thenReturn(opt_expected);
        Optional<byte[]> actual = fileOps.read(path, expected.length + 1, 1);

        assertFalse(actual.isPresent());
    }

    @Test
    public void readValidOffsetValidCount() throws IOException {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);

        when(stub.requestFile(path)).thenReturn(opt_expected);
        Optional<byte[]> actual = fileOps.read(path, 0, expected.length);

        assertTrue(actual.isPresent());
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void readValidOffsetInvalidCount() throws IOException {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);


        when(stub.requestFile(path)).thenReturn(opt_expected);
        Optional<byte[]> actual = fileOps.read(path, 1, expected.length + 1);

        assertTrue(actual.isPresent());
        assertArrayEquals(Arrays.copyOfRange(expected, 1, expected.length), actual.get());
    }

    @Test
    public void readInvalidOffsetInvalidCount() throws IOException {
        String path = "file.txt";
        byte[] expected = "abc".getBytes();
        Optional<byte[]> opt_expected = Optional.of(expected);

        when(stub.requestFile(path)).thenReturn(opt_expected);
        Optional<byte[]> actual = fileOps.read(path, expected.length + 1, -1);

        assertFalse(actual.isPresent());
    }
}
