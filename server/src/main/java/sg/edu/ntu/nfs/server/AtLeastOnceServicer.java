package sg.edu.ntu.nfs.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.*;
import sg.edu.ntu.nfs.common.responses.*;
import sg.edu.ntu.nfs.common.values.Str;
import sg.edu.ntu.nfs.common.values.Value;
import sg.edu.ntu.nfs.common.exceptions.BadRequestException;
import sg.edu.ntu.nfs.common.exceptions.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
                    .map(Path::getFileName)
                    .map(filepath -> {
                        StringBuilder pathString = new StringBuilder(filepath.toString());
                        if (Files.isDirectory(filepath))
                            pathString.append('/');
                        return new Str(pathString.toString());
                    })
                    .collect(Collectors.toList());
            return new GenericResponse(ResponseStatus.OK, res);
        } catch (NotDirectoryException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }
}
