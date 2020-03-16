package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Value;

import java.io.InvalidClassException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRequest implements Request {
    private final RequestId id;
    private final RequestType name;
    private final int numParams;
    private List<Value> params;

    protected AbstractRequest(RequestType name, int numParams) {
        this(new RequestId(), name, numParams);
    }

    protected AbstractRequest(RequestId id, RequestType name, int numParams) {
        this.id = id;
        this.name = name;
        this.numParams = numParams;
        this.params = new ArrayList<>(numParams);
    }

    protected Value getParam(int pos) {
        return params.get(pos);
    }

    protected void addParam(Value val) {
        if (params.size() >= numParams)
            throw new IllegalArgumentException(String.format("Parameter length exceeds %d", numParams));
        params.add(val);
    }

    protected void setParam(int pos, Value val) {
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
                .putInt(name.ordinal())
                .putInt(numParams);
        for (Value val : params) {
            payload.put(val.toBytes());
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
    public RequestType getName() {
        return name;
    }
}
