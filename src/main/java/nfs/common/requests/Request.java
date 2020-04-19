package nfs.common.requests;

import nfs.common.Serializer;
import nfs.common.values.Value;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

public interface Request extends Serializer {
    /**
     * Get the request ID. This ID does not include IP and port information.
     * Transport information is extracted through Datagram.
     *
     * @return request ID.
     */
    int getId();

    /**
     * Get the name of the request.
     *
     * @return request name.
     */
    RequestName getName();

    /**
     * Get total number of parameters
     *
     * @return number of parameters.
     */
    int getNumParams();

    class Builder {
        private static final RequestName[] REQUEST_NAMES = RequestName.values();

        public static Request parseFrom(byte[] data) throws InvalidObjectException {
            ByteBuffer buf = ByteBuffer.wrap(data);
            RequestId id = new RequestId(buf.getInt());
            int requestNameIndex = buf.get();
            if (requestNameIndex < 0 || requestNameIndex >= REQUEST_NAMES.length)
                throw new InvalidObjectException("Unable to parse request: no matching request name.");
            RequestName name = REQUEST_NAMES[requestNameIndex];
            int numParams = buf.getInt();
            AbstractRequest request = name.build(id);

            if (numParams != request.getNumParams())
                throw new InvalidObjectException(String.format(
                        "Unable to parse request %s: wrong number of parameters. Expected: %d, Actual: %d .",
                        name.name(), request.getNumParams(), numParams));
            for (int i = 0; i < numParams; i++) {
                request.addParam(Value.Builder.parseFrom(buf));
            }
            return request;
        }
    }
}
