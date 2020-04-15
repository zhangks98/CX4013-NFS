package nfs.common.responses;

import nfs.common.values.Value;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class GenericResponse implements Response {
    private final int reqId;
    private final ResponseStatus status;
    private final List<Value> values;

    public GenericResponse(int reqId, ResponseStatus status) {
        this(reqId, status, Collections.emptyList());
    }

    public GenericResponse(int reqId, ResponseStatus status, List<Value> values) {
        this.reqId = reqId;
        this.status = status;
        this.values = values;
    }

    @Override
    public int getReqId() {
        return reqId;
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
        payload.putInt(reqId)
                .put((byte) status.ordinal())
                .putInt(numValues);
        for (Value val : values) {
            val.putBytes(payload);
        }
        return payload.array();
    }
}
