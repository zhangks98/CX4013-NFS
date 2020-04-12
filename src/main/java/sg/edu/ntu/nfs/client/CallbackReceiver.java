package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.requests.RequestBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class CallbackReceiver implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger();

    private DatagramSocket socket;
    private CallbackHandler callbackHandler;

    public CallbackReceiver() {}

    @Override
    public Integer call() throws Exception {
        socket = new DatagramSocket();
        callbackHandler = new CallbackHandler();
        logger.info("Callback receiver running ...");
        serve();
        return 0;
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
