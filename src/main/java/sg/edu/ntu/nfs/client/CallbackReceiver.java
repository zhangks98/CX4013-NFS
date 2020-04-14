package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.exceptions.BadRequestException;
import sg.edu.ntu.nfs.common.requests.FileUpdatedCallback;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.requests.RequestBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class CallbackReceiver {
    private static final Logger logger = LogManager.getLogger();
    private DatagramSocket socket;
    private CacheHandler cacheHandler;

    public CallbackReceiver(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    public void run(int monitorInterval) {
        try {
            socket = new DatagramSocket();
            serve(monitorInterval);
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * Receive callbacks from the server and pass to the callback handler
     * @param monitorInterval monitor interval used as timeout duration
     * @throws IOException
     */
    private void serve(int monitorInterval) throws IOException {
        while (true) {
            try{
                DatagramPacket request = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
                socket.setSoTimeout(monitorInterval);
                socket.receive(request);

                // Unmarshal the callback request.
                Request req = RequestBuilder.parseFrom(request.getData());
                logger.info(String.format("Received: %s", req.getName()));

                try {
                    // handle the request
                    handle(req);
                } catch (Exception e) {
                    logger.error(String.format("Error handling %s", req.getName()), e);
                }
            } catch (SocketTimeoutException ste) {
                logger.info("End of monitor interval");
                return;
            }
        }
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
