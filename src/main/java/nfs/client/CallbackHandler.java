package nfs.client;

import nfs.common.Serializer;
import nfs.common.exceptions.BadRequestException;
import nfs.common.requests.FileUpdatedCallback;
import nfs.common.requests.Request;
import nfs.common.requests.RequestBuilder;
import nfs.common.requests.RequestName;
import nfs.common.responses.Response;
import nfs.common.responses.ResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class CallbackHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    private static final int CALLBACK_REQ_ID = 0;
    private final CacheHandler cacheHandler;
    private final DatagramSocket socket;
    private final BlockingQueue<Response> queue;

    public CallbackHandler(CacheHandler cacheHandler, DatagramSocket socket,
                           BlockingQueue<Response> queue) {
        this.cacheHandler = cacheHandler;
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[Serializer.BUF_SIZE],
                        Serializer.BUF_SIZE);
                socket.receive(packet);
                byte[] data = packet.getData();
                int req_id = ByteBuffer.wrap(data).getInt();

                if (req_id == CALLBACK_REQ_ID) {
                    // If the packet is a callback request.
                    Request req = RequestBuilder.parseFrom(data);
                    if (req.getName() != RequestName.FILE_UPDATED)
                        throw new BadRequestException(
                                "Received a request with id 0, but it is not a FileUpdatedCallback");
                    FileUpdatedCallback callback = (FileUpdatedCallback) req;
                    logger.info(String.format("Received FileUpdatedCallback for %s", callback.getPath()));
                    handle(callback);
                } else {
                    // Else it is a response of a register request.
                    Response res = ResponseBuilder.parseFrom(data);
                    // Offer the response to the Proxy.
                    queue.offer(res);
                }
            } catch (SocketException e) {
                break;
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     * Handle FileUpdated callbacks
     *
     * @param callback FileUpdated callback
     */
    void handle(FileUpdatedCallback callback) {
        String filePath = callback.getPath();
        byte[] data = callback.getData();
        cacheHandler.updateFile(filePath, data);
    }
}
