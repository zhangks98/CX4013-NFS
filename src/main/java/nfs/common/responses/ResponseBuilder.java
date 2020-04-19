package nfs.common.responses;

import nfs.common.values.Value;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ResponseBuilder {
    private static final ResponseStatus[] RESPONSE_STATUSES = ResponseStatus.values();

    public static Response parseFrom(byte[] data) throws InvalidObjectException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        int reqId = buf.getInt();
        int statusIndex = buf.get();
        ResponseStatus status;
        if (statusIndex < 0 || statusIndex >= RESPONSE_STATUSES.length)
            status = ResponseStatus.UNKNOWN;
        else
            status = RESPONSE_STATUSES[statusIndex];
        int numValues = buf.getInt();
        List<Value> values = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            values.add(Value.Builder.parseFrom(buf));
        }
        return new GenericResponse(reqId, status, values);
    }
}
