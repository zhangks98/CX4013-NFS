package nfs.common.requests;

import org.junit.Test;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        serialized.putInt(5, 2);
        GetAttrRequest actual = (GetAttrRequest) Request.Builder.parseFrom(serialized.array());
    }

    @Test
    public void marshallEmptyRequest() throws InvalidClassException, InvalidObjectException {
        Request expected = new EmptyRequest();
        byte[] serialized = expected.toBytes();
        Request actual = Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(0, actual.getNumParams());
    }

    @Test
    public void marshallGetAttrRequest() throws InvalidClassException, InvalidObjectException {
        String path = "hello.txt";
        GetAttrRequest expected = new GetAttrRequest(path);
        byte[] serialized = expected.toBytes();
        GetAttrRequest actual = (GetAttrRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.GET_ATTR, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallReadRequest() throws InvalidClassException, InvalidObjectException {
        String path = "world.txt";
        ReadRequest expected = new ReadRequest(path);
        byte[] serialized = expected.toBytes();
        ReadRequest actual = (ReadRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.READ, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallInsertRequest() throws InvalidClassException, InvalidObjectException {
        String path = "abc.txt";
        int offset = 1;
        byte[] data = new byte[]{0xd, 0xe, 0xf};
        InsertRequest expected = new InsertRequest(path, offset, data);
        byte[] serialized = expected.toBytes();
        InsertRequest actual = (InsertRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.INSERT, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(offset, actual.getOffset());
        assertArrayEquals(data, actual.getData());
    }

    @Test
    public void marshallAppendRequest() throws InvalidClassException, InvalidObjectException {
        String path = "abc.txt";
        byte[] data = new byte[]{0xd, 0xe, 0xf};
        AppendRequest expected = new AppendRequest(path, data);
        byte[] serialized = expected.toBytes();
        AppendRequest actual = (AppendRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.APPEND, actual.getName());
        assertEquals(path, actual.getPath());
        assertArrayEquals(data, actual.getData());
    }

    @Test
    public void marshallListDirRequest() throws InvalidClassException, InvalidObjectException {
        String path = "/src/test";
        ListDirRequest expected = new ListDirRequest(path);
        byte[] serialized = expected.toBytes();
        ListDirRequest actual = (ListDirRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.LIST_DIR, actual.getName());
        assertEquals(path, actual.getPath());
    }

    @Test
    public void marshallTouchRequest() throws InvalidClassException, InvalidObjectException {
        String path = "test.txt";
        TouchRequest expected = new TouchRequest("test.txt");
        byte[] serialized = expected.toBytes();
        TouchRequest actual = (TouchRequest) Request.Builder.parseFrom(serialized);
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
        RegisterRequest actual = (RegisterRequest) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.REGISTER, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(monitorInterval, actual.getMonitorInterval());
    }

    @Test
    public void marshallFileUpdatedRequest() throws InvalidClassException, InvalidObjectException {
        String path = "test.txt";
        long mtime = 123;
        byte[] data = new byte[]{(byte) 0xdd, (byte) 0xee, (byte) 0xff};
        FileUpdatedCallback expected = new FileUpdatedCallback(path, mtime, data);
        byte[] serialized = expected.toBytes();
        FileUpdatedCallback actual = (FileUpdatedCallback) Request.Builder.parseFrom(serialized);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(RequestName.FILE_UPDATED, actual.getName());
        assertEquals(path, actual.getPath());
        assertEquals(mtime, actual.getMtime());
        assertArrayEquals(data, actual.getData());
    }
}