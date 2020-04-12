package sg.edu.ntu.nfs.client;

import sg.edu.ntu.nfs.common.exceptions.BadRequestException;
import sg.edu.ntu.nfs.common.requests.FileUpdatedCallback;
import sg.edu.ntu.nfs.common.requests.Request;

public class CallbackHandler {
    // use dummy values since these two params are not required for file updates
    private Proxy dummy_stub = null;
    private long dummy_fresh_t = 0;
    private CacheHandler cacheHandler;

    public CallbackHandler(){
        this.cacheHandler = new CacheHandler(dummy_stub, dummy_fresh_t);
    }

    public void handle(Request request) throws Exception {
        switch (request.getName()) {
            case FILE_UPDATED:
                handle((FileUpdatedCallback) request);
            default:
                throw new BadRequestException("Request name not found.");
        }
    }

    void handle(FileUpdatedCallback callback) {
        String filepath = callback.getPath();
        byte[] data = callback.getData();
        cacheHandler.updateFile(filepath, data);
    }

}
