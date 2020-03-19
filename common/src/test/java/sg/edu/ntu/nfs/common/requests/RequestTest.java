package sg.edu.ntu.nfs.common.requests;

import org.junit.Test;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class RequestTest {

    @Test
    public void incrementedReqestId() {
        Request req1 = new EmptyRequest();
        Request req2 = new EmptyRequest();
        assertEquals(req1.getId() + 1, req2.getId());
    }

    @Test(expected = InvalidClassException.class)
    public void shouldNotMarshallMismatchedParam() throws InvalidClassException {
        GetAttrRequest expected = new GetAttrRequest(new RequestId(0));
        byte[] serialized = expected.toBytes();
    }

    @Test(expected = InvalidObjectException.class)
    public void shouldNotUnmarshalMismatchedParam() throws InvalidClassException, InvalidObjectException {
        GetAttrRequest expected = new GetAttrRequest("hello.txt");
        ByteBuffer serialized = ByteBuffer.wrap(expected.toBytes());
        // Sets numParams to 2 in serialized message.
        serialized.putInt(8, 2);
        GetAttrRequest actual = (GetAttrRequest) RequestBuilder.parseFrom(serialized);
    }

    @Test
    public void marshallEmptyRequest() throws InvalidClassException, InvalidObjectException {
        Request expected = new EmptyRequest();
        byte[] serialized = expected.toBytes();
        Request actual = RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(0, actual.getNumParams());
    }

    @Test
    public void marshallGetAttrRequest() throws InvalidClassException, InvalidObjectException {
        String path = "hello.txt";
        GetAttrRequest expected = new GetAttrRequest(path);
        byte[] serialized = expected.toBytes();
        GetAttrRequest actual = (GetAttrRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.GET_ATTR, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallReadRequest() throws InvalidClassException, InvalidObjectException {
        String path = "world.txt";
        int offset = 1;
        int count = 2;
        ReadRequest expected = new ReadRequest(path, offset, count);
        byte[] serialized = expected.toBytes();
        ReadRequest actual = (ReadRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.READ, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(offset, actual.getOffset());
        assertEquals(count, actual.getCount());
    }

    @Test
    public void marshallWriteRequest() throws InvalidClassException, InvalidObjectException {
        String path = "abc.txt";
        int offset = 1;
        int count = 2;
        byte[] data = new byte[]{0xd, 0xe, 0xf};
        WriteRequest expected = new WriteRequest(path, offset, count, data);
        byte[] serialized = expected.toBytes();
        WriteRequest actual = (WriteRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.WRITE, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(offset, actual.getOffset());
        assertEquals(count, actual.getCount());
        assertArrayEquals(data, actual.getData());
    }

    @Test
    public void marshallListDirRequest() throws InvalidClassException, InvalidObjectException {
        String path = "/src/test";
        ListDirRequest expected = new ListDirRequest(path);
        byte[] serialized = expected.toBytes();
        ListDirRequest actual = (ListDirRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.LIST_DIR, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallTouchRequest() throws InvalidClassException, InvalidObjectException {
        String path = "test.txt";
        TouchRequest expected = new TouchRequest("test.txt");
        byte[] serialized = expected.toBytes();
        TouchRequest actual = (TouchRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.TOUCH, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallRegisterRequest() throws InvalidClassException, InvalidObjectException {
        String path = "test.txt";
        int monitorInterval = 100;
        RegisterRequest expected = new RegisterRequest(path, monitorInterval);
        byte[] serialized = expected.toBytes();
        RegisterRequest actual = (RegisterRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.REGISTER, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(monitorInterval, actual.getMonitorInterval());
    }

    @Test
    public void marshallFileUpdatedRequest() throws InvalidClassException, InvalidObjectException {
        String path = "test.txt";
        byte[] data = new byte[]{(byte) 0xdd, (byte) 0xee, (byte) 0xff};
        FileUpdatedRequest expected = new FileUpdatedRequest(path, data);
        byte[] serialized = expected.toBytes();
        FileUpdatedRequest actual = (FileUpdatedRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.FILE_UPDATED, actual.getName());
        assertEquals(path, actual.getPath());
        assertArrayEquals(data, actual.getData());
    }
}