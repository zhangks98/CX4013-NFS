package sg.edu.ntu.nfs.common.requests;

import org.junit.Test;
import sg.edu.ntu.nfs.common.values.Int64;

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
        GetAttrRequest expected = new GetAttrRequest("hello.txt");
        byte[] serialized = expected.toBytes();
        GetAttrRequest actual = (GetAttrRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
    }

    @Test
    public void marshallReadRequest() throws InvalidClassException, InvalidObjectException {
        ReadRequest expected = new ReadRequest("world.txt", 1, 2);
        byte[] serialized = expected.toBytes();
        ReadRequest actual = (ReadRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getOffset(), actual.getOffset());
        assertEquals(expected.getCount(), actual.getCount());
    }

    @Test
    public void marshallWriteRequest() throws InvalidClassException, InvalidObjectException {
        WriteRequest expected = new WriteRequest("abc.txt", 1, 2, new byte[]{0xd, 0xe, 0xf});
        byte[] serialized = expected.toBytes();
        WriteRequest actual = (WriteRequest) RequestBuilder.parseFrom(ByteBuffer.wrap(serialized));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getOffset(), actual.getOffset());
        assertEquals(expected.getCount(), actual.getCount());
        assertArrayEquals(expected.getData(), actual.getData());
    }
}