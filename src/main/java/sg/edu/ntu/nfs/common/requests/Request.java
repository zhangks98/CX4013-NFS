package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Response;
import sg.edu.ntu.nfs.common.Serializer;
import sg.edu.ntu.nfs.common.values.Value;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class Request implements Serializer {
    private static final RequestName[] REQUEST_NAMES = RequestName.values();
    private static int nextId = 0;
    private final int id;
    private final RequestName name;
    private final int numParams;
    private List<Value> params;

    protected Request(RequestName name, int numParams) {
        this(nextId++, name, numParams);
    }

    protected Request(int id, RequestName name, int numParams) {
        this.id = id;
        this.name = name;
        this.numParams = numParams;
        this.params = new ArrayList<>(numParams);
    }

    public static Request parseFrom(ByteBuffer data) throws InvalidObjectException {
        int id = data.getInt();
        int requstNameInd = data.getInt();
        if (requstNameInd > REQUEST_NAMES.length)
            throw new InvalidObjectException("Unable to parse request: request name index out of bound.");
        RequestName name = REQUEST_NAMES[requstNameInd];
        int numParams = data.getInt();
        Request request;
        switch (name) {
            case READ:
                request = new ReadRequest(id);
                break;
            case WRITE:
                request = new WriteRequest(id);
                break;
            case GETATTR:
                request = new GetAttrRequest(id);
                break;
            default:
                throw new InvalidObjectException("Unable to parse request: no matching request name");
        }

        if (numParams != request.getNumParams())
            throw new InvalidObjectException(String.format(
                    "Unable to parse request %s: wrong number of parameters. Expected: %d, Actual: %d .",
                    name.name(), numParams, request.getNumParams()));
        for (int i = 0; i < numParams; i++) {
            request.setParam(i, Value.parseFrom(data));
        }
        return request;
    }

    protected void setParam(int pos, Value val) {
        params.set(pos, val);
    }

    public abstract Response eval();

    @Override
    public byte[] toByte() throws InvalidClassException {
        if (numParams != params.size())
            throw new InvalidClassException(String.format(
                    "Unable to serialize request %s: wrong number of parameters. Expected: %d, Actual: %d .",
                    name.name(), numParams, params.size()));
        ByteBuffer payload = ByteBuffer.allocate(BUF_SIZE);
        payload.putInt(id)
                .putInt(name.ordinal())
                .putInt(numParams);
        for (Value val : params) {
            payload.put(val.toByte());
        }
        return payload.array();
    }

    public int getNumParams() {
        return numParams;
    }
}
