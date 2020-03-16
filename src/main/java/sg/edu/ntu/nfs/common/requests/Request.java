package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Serializer;

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
    RequestType getName();

    /**
     * Get total number of parameters
     *
     * @return number of parameters.
     */
    int getNumParams();
}
