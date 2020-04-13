package sg.edu.ntu.nfs.client;

import sg.edu.ntu.nfs.common.exceptions.BadRequestException;
import sg.edu.ntu.nfs.common.requests.FileUpdatedCallback;
import sg.edu.ntu.nfs.common.requests.Request;

public class CallbackHandler {
    private CacheHandler cacheHandler;

    public CallbackHandler(CacheHandler cacheHandler){
        this.cacheHandler = cacheHandler;
    }

    /**
     * Pass a callback to its corresponding handler
     * @param request
     * @throws Exception
     */
    public void handle(Request request) throws Exception {
        switch (request.getName()) {
            case FILE_UPDATED:
                handle((FileUpdatedCallback) request);
            default:
                throw new BadRequestException("Request name not found.");
        }
    }

    /**
     * Handle FileUpdated callbacks
     * @param callback FileUpdated callback
     */
    void handle(FileUpdatedCallback callback) {
        String filePath = callback.getPath();
        byte[] data = callback.getData();
        cacheHandler.updateFile(filePath, data);
    }

}
