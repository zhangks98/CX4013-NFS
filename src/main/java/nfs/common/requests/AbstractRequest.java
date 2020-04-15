package nfs.common.requests;

import nfs.common.values.Value;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractRequest implements Request {
    private final RequestId id;
    private final RequestName name;
    private final int numParams;
    private final List<Value> params;

    AbstractRequest(RequestName name) {
        this(new RequestId(), name);
    }

    AbstractRequest(RequestId id, RequestName name) {
        this.id = id;
        this.name = name;
        this.numParams = name.numParams();
        this.params = new ArrayList<>(numParams);
    }

    Value getParam(int pos) {
        return params.get(pos);
    }

    void addParam(Value val) {
        if (params.size() >= numParams)
            throw new IllegalArgumentException(String.format("Parameter length exceeds %d", numParams));
        params.add(val);
    }

    void setParam(int pos, Value val) {
        params.set(pos, val);
    }

    @Override
    public byte[] toBytes() throws InvalidClassException {
        if (numParams != params.size())
            throw new InvalidClassException(String.format(
                    "Unable to serialize request %s: wrong number of parameters. Expected: %d, Actual: %d .",
                    name.name(), numParams, params.size()));
        ByteBuffer payload = ByteBuffer.allocate(BUF_SIZE);
        payload.putInt(id.getId())
                .put((byte) name.ordinal())
                .putInt(numParams);
        for (Value val : params) {
            val.putBytes(payload);
        }
        return payload.array();
    }

    @Override
    public int getNumParams() {
        return numParams;
    }

    @Override
    public int getId() {
        return id.getId();
    }

    @Override
    public RequestName getName() {
        return name;
    }

}
