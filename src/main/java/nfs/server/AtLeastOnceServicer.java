package nfs.server;

import nfs.common.exceptions.BadRequestException;
import nfs.common.exceptions.NotFoundException;
import nfs.common.requests.ListDirRequest;
import nfs.common.requests.Request;
import nfs.common.responses.GenericResponse;
import nfs.common.responses.Response;
import nfs.common.responses.ResponseStatus;
import nfs.common.values.Str;
import nfs.common.values.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class AtLeastOnceServicer implements Servicer {

    private static final Logger logger = LogManager.getLogger();
    File rootDir;

    public AtLeastOnceServicer(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public Response handle(Request request, Context context) throws Exception {
        switch (request.getName()) {
            case LIST_DIR:
                return handle((ListDirRequest) request);
            default:
                throw new BadRequestException("Request name not found.");
        }
    }

    Response handle(ListDirRequest request) throws IOException, NotFoundException {
        String dirName = request.getPath();
        Path path = Paths.get(rootDir.getPath(), dirName);
        try {
            List<Value> res = Files.list(path)
                    .map(filepath -> {
                        StringBuilder pathStringBuilder = new StringBuilder(
                                filepath.getFileName().toString());
                        if (Files.isDirectory(filepath))
                            pathStringBuilder.append('/');
                        return new Str(pathStringBuilder.toString());
                    })
                    .collect(Collectors.toList());
            return new GenericResponse(request.getId(), ResponseStatus.OK, res);
        } catch (NotDirectoryException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }
}
