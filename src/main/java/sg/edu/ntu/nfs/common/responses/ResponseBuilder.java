package sg.edu.ntu.nfs.common.responses;

import sg.edu.ntu.nfs.common.values.Value;
import sg.edu.ntu.nfs.common.values.ValueBuilder;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ResponseBuilder {
    private static final ResponseStatus[] RESPONSE_STATUSES = ResponseStatus.values();

    public static Response parseFrom(ByteBuffer data) throws InvalidObjectException {
        int statusIndex = data.getInt();
        ResponseStatus status;
        if (statusIndex < 0 || statusIndex >= RESPONSE_STATUSES.length)
            status = ResponseStatus.UNKNOWN;
        else
            status = RESPONSE_STATUSES[statusIndex];
        int numValues = data.getInt();
        List<Value> values = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            values.add(ValueBuilder.parseFrom(data));
        }
        return new GenericResponse(status, values);
    }
}
