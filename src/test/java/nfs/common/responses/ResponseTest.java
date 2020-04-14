package nfs.common.responses;

import nfs.common.values.Int32;
import org.junit.Test;
import nfs.common.values.Int64;
import nfs.common.values.Value;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ResponseTest {

    @Test
    public void emptyResponse() throws InvalidClassException, InvalidObjectException {
        List<Value> valueList = new ArrayList<>();
        int reqId = 1;
        Response expected = new GenericResponse(reqId, ResponseStatus.NOT_FOUND);
        byte[] serialized = expected.toBytes();
        Response actual = ResponseBuilder.parseFrom(serialized);
        assertEquals(reqId, actual.getReqId());
        assertEquals(0, actual.getValues().size());
    }

    @Test
    public void responseMarshalling() throws InvalidClassException, InvalidObjectException {
        List<Value> valueList = new ArrayList<>();
        valueList.add(new Int64(1L));
        valueList.add(new Int32(2));
        Response expected = new GenericResponse(0, ResponseStatus.OK, valueList);
        byte[] serialized = expected.toBytes();
        Response actual = ResponseBuilder.parseFrom(serialized);
        assertEquals(0, actual.getReqId());
        assertEquals(1L, (long) valueList.get(0).getVal());
        assertEquals(2, (int) valueList.get(1).getVal());
    }
}