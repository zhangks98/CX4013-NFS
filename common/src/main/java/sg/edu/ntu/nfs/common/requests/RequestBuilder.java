package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.ValueBuilder;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

public class RequestBuilder {
    private static final RequestName[] REQUEST_NAMES = RequestName.values();

    public static Request parseFrom(byte[] data) throws InvalidObjectException {
        ByteBuffer buf = ByteBuffer.wrap(data);
        RequestId id = new RequestId(buf.getInt());
        int requestNameIndex = buf.getInt();
        if (requestNameIndex < 0 || requestNameIndex >= REQUEST_NAMES.length)
            throw new InvalidObjectException("Unable to parse request: request name index out of bound.");
        RequestName name = REQUEST_NAMES[requestNameIndex];
        int numParams = buf.getInt();
        AbstractRequest request;
        switch (name) {
            case EMPTY:
                request = new EmptyRequest(id);
                break;
            case READ:
                request = new ReadRequest(id);
                break;
            case WRITE:
                request = new WriteRequest(id);
                break;
            case GET_ATTR:
                request = new GetAttrRequest(id);
                break;
            case LIST_DIR:
                request = new ListDirRequest(id);
                break;
            case TOUCH:
                request = new TouchRequest(id);
                break;
            case REGISTER:
                request = new RegisterRequest(id);
                break;
            case FILE_UPDATED:
                request = new FileUpdatedRequest(id);
                break;
            default:
                throw new InvalidObjectException("Unable to parse request: no matching request name");
        }

        if (numParams != request.getNumParams())
            throw new InvalidObjectException(String.format(
                    "Unable to parse request %s: wrong number of parameters. Expected: %d, Actual: %d .",
                    name.name(), request.getNumParams(), numParams));
        for (int i = 0; i < numParams; i++) {
            request.addParam(ValueBuilder.parseFrom(buf));
        }
        return request;
    }
}
