package sg.edu.ntu.nfs.common.responses;

import sg.edu.ntu.nfs.common.values.Value;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class GenericResponse implements Response {
    private final ResponseStatus status;
    private final List<Value> values;

    public GenericResponse(ResponseStatus status) {
        this(status, Collections.emptyList());
    }

    public GenericResponse(ResponseStatus status, List<Value> values) {
        this.status = status;
        this.values = values;
    }

    @Override
    public ResponseStatus getStatus() {
        return status;
    }

    @Override
    public List<Value> getValues() {
        return values;
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        int numValues = values.size();
        ByteBuffer payload = ByteBuffer.allocate(BUF_SIZE);
        payload.putInt(status.ordinal())
                .putInt(numValues);
        for (Value val : values) {
            payload.put(val.toBytes());
        }
        return payload.array();
    }
}
