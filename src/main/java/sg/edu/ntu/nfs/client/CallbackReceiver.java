package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.requests.RequestBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class CallbackReceiver extends Thread {
    private static final Logger logger = LogManager.getLogger();
    private DatagramSocket socket;
    private CallbackHandler callbackHandler;
    private CacheHandler cacheHandler;

    public CallbackReceiver(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    /**
     * Close the socket to terminate the thread
     */
    public void stopListening() {
        socket.close();
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            callbackHandler = new CallbackHandler(cacheHandler);
            logger.info("Started callback receiver thread ...");
            serve();
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * Receive callbacks from the server and pass to the callback handler
     * @throws IOException
     */
    private void serve() throws IOException {
        while (true) {
            DatagramPacket request = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
            socket.receive(request);

            // Unmarshal the callback request.
            Request req = RequestBuilder.parseFrom(request.getData());
            logger.info(String.format("Received: %s", req.getName()));

            try {
                // handle the request
                callbackHandler.handle(req);
            } catch (Exception exp) {
                logger.error(String.format("Error handling %s", req.getName()), exp);
            }
        }
    }
}
