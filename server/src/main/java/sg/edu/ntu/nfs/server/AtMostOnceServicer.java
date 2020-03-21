package sg.edu.ntu.nfs.server;

import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.GenericResponse;
import sg.edu.ntu.nfs.common.responses.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.responses.ResponseStatus;
import sg.edu.ntu.nfs.common.values.Str;
import sg.edu.ntu.nfs.common.values.Value;
import sg.edu.ntu.nfs.server.exceptions.BadRequestException;
import sg.edu.ntu.nfs.server.exceptions.NotFoundException;

public class AtMostOnceServicer implements Servicer {

    private static final Logger logger = LogManager.getLogger();
    private File rootDir;
    private HashMap<Integer, Response> requestIdToResponseMap;

    public AtMostOnceServicer(File rootDir) {
        this.rootDir = rootDir;
        this.requestIdToResponseMap = new HashMap<>();
    }

    @Override
    public Response handle(Request request, Context context) throws Exception {
        // TODO(Ming): check timestamp of response (maybe associate it with Response, or as part of the map), so that response generated too long ago may be discarded
        // TODO(Ming): delete outdated/retransmitted response from map to prevent from infinitely growing
        // TODO(Ming): refactor duplicate check into a separate method?
        // Check if it is a duplicate request
        Response storedResponse = requestIdToResponseMap.get(request.getId());
        if (storedResponse != null) {
            // Return stored reply message
            return storedResponse;
        }
        Response res;
        switch (request.getName()) {
            case LIST_DIR:
                res = handle((ListDirRequest) request);
                break;
            default:
                throw new BadRequestException("Request name not found.");
        }
        // Store the response to requestIdToResponseMap
        requestIdToResponseMap.put(request.getId(), res);
        return res;
    }

    Response handle(ListDirRequest request) throws IOException, NotFoundException {
        // TODO: duplicate code fragment
        String dirName = request.getPath();
        Path path = Paths.get(rootDir.getPath(), dirName);
        try {
            List<Value> res = Files.list(path)
                    .map(file -> new Str(file.normalize().toString()))
                    .collect(Collectors.toList());
            return new GenericResponse(ResponseStatus.OK, res);
        } catch (NotDirectoryException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }
}
