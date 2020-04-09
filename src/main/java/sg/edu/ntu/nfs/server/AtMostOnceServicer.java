package sg.edu.ntu.nfs.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.Response;

import java.io.File;
import java.util.HashMap;

public class AtMostOnceServicer extends AtLeastOnceServicer {

    private static final Logger logger = LogManager.getLogger();
    private HashMap<String, Response> requestToResponseMap;

    public AtMostOnceServicer(File rootDir) {
        super(rootDir);
        this.requestToResponseMap = new HashMap<>();
    }

    @Override
    public Response handle(Request request, Context context) throws Exception {
        // TODO(Ming): check timestamp of response (maybe associate it with Response, or as part of the map), so that response generated too long ago may be discarded
        // TODO(Ming): delete outdated/retransmitted response from map to prevent from infinitely growing
        // TODO(Ming): refactor duplicate check into a separate method?
        String requestIdentifier = context.getAddress().toString() + ":" + context.getPort() + ":" + request.getId();
        // Check if it is a duplicate request
        Response storedResponse = requestToResponseMap.get(requestIdentifier);
        if (storedResponse != null) {
            // Return stored reply message
            return storedResponse;
        }
        Response res = super.handle(request, context);
        // Store the response to requestIdToResponseMap
        requestToResponseMap.put(requestIdentifier, res);
        return res;
    }
}
