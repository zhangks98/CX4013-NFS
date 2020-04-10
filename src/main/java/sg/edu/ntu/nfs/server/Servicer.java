package sg.edu.ntu.nfs.server;

import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.Response;

import java.io.IOException;

public interface Servicer {
    /**
     * RPC handler.
     *
     * @param request the pending request.
     * @param context the request context of the client.
     * @return the response of the request.
     */
    Response handle(Request request, Context context) throws Exception;
}
