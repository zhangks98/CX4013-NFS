package nfs.client;

import nfs.common.Serializer;
import nfs.common.requests.*;
import nfs.common.responses.Response;
import nfs.common.responses.ResponseStatus;
import nfs.common.values.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Proxy {
    private static final Logger logger = LogManager.getLogger();
    private final InetAddress address;
    private final int port;
    private final DatagramSocket socket;
    private final DatagramSocket callbackSocket;
    private final BlockingQueue<Response> queue;
    private final int timeout = 5000; // in milliseconds
    private final int maxRecvAttempts = 5;
    private final double lossProb;

    public Proxy(InetAddress address, int port, DatagramSocket callbackSocket,
                 BlockingQueue<Response> queue, double lossProb) throws SocketException {
        this.address = address;
        this.port = port;
        this.callbackSocket = callbackSocket;
        this.queue = queue;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(timeout);
        this.lossProb = lossProb;
    }

    /**
     * Send a read request to the server
     *
     * @param filePath file path on server
     * @return file content in bytes
     */
    public Optional<byte[]> requestFile(String filePath) throws IOException {
        return invoke(new ReadRequest(filePath)).map(res -> (byte[]) res.get(0));
    }

    /**
     * Send an insert request to the server
     * print the number of bytes written
     *
     * @param filePath file path on server
     * @param offset   offset of content insertion, measured in number of bytes
     * @param data     bytes to write
     */
    public void insert(String filePath, int offset, byte[] data) throws IOException {
        invoke(new InsertRequest(filePath, offset, data))
                .ifPresent(res -> System.out.println("Success"));
    }

    /**
     * Send an append request to the server
     * print the number of bytes written
     *
     * @param filePath file path on server
     * @param data     bytes to write
     */
    public void append(String filePath, byte[] data) throws IOException {
        invoke(new AppendRequest(filePath, data))
                .ifPresent(res -> System.out.println("Success"));
    }

    /**
     * Request to touch a file on the server
     * if file exists, last modified time of the file will be returned
     * otherwise, file will be created on the server
     *
     * @param filePath file path on server
     */
    public void touch(String filePath) throws IOException {
        invoke(new TouchRequest(filePath)).ifPresent(res -> {
            long atime = (long) res.get(0);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(atime), ZoneId.systemDefault());
            System.out.println(filePath + " last accessed at: " + dateTime);
        });
    }

    /**
     * Send a request to list the contents of a directory on the server
     *
     * @param dir directory of interest on server
     */
    public void listDir(String dir) throws IOException {
        invoke(new ListDirRequest(dir)).ifPresent(res -> {
            for (Object filename : res) {
                System.out.println(filename);
            }
        });
    }

    /**
     * Request the last access time and last modified time of a file on the server
     *
     * @param filePath file path on server
     * @return last modified time and last access time of the file
     */
    public Optional<long[]> getAttr(String filePath) throws IOException {
        return invoke(new GetAttrRequest(filePath)).map(res -> {
            long mtime = (long) res.get(0);
            long atime = (long) res.get(1);
            return new long[]{mtime, atime};
        });
    }

    /**
     * Send a request to register for updates of a file on server
     *
     * @param filePath        file path on server
     * @param monitorInterval duration for monitor file updates
     */
    public void register(String filePath, int monitorInterval) throws IOException {
        filePath = Paths.get(filePath).normalize().toString();
        if (monitorInterval <= 0) {
            logger.warn("monitorInterval should be greater than 0.");
            return;
        }
        Request request = new RegisterRequest(filePath, monitorInterval);
        DatagramPacket req = new DatagramPacket(request.toBytes(), Serializer.BUF_SIZE, address, port);
        callbackSocket.send(req);
        try {
            Response res = queue.poll(timeout, TimeUnit.MILLISECONDS);
            if (res == null) {
                logger.warn(String.format(
                        "Callback register error: No response received after %d ms.", timeout));
            } else if (res.getStatus() == ResponseStatus.OK) {
                System.out.println("Success");
            } else {
                String errorMsg = res.getValues().size() > 0 ?
                        (String) res.getValues().get(0).getVal() : "";
                logger.warn(String.format("Callback register error: response status %s: %s",
                        res.getStatus(), errorMsg));
            }
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    /**
     * Close all the sockets.
     */
    public void close() {
        socket.close();
        callbackSocket.close();
    }

    /**
     * Determine if a request loss will be simulated
     *
     * @return boolean indicating if a request is lost
     */
    private boolean requestLost() {
        Random rand = new Random();
        double randNum = rand.nextDouble();
        return randNum < lossProb;
    }

    /**
     * Invoke a remote procedure on the server and get response
     * If timeout, retry within maximum possible attempts
     *
     * @param request client request
     * @return server response
     */
    private Optional<List<Object>> invoke(Request request) throws IOException {
        int count = 0;

        // Marshall and send the request.
        DatagramPacket req = new DatagramPacket(request.toBytes(), Serializer.BUF_SIZE, address, port);

        while (true) {
            try {
                if (!requestLost())
                    socket.send(req);
                else
                    logger.warn("The " + request.getName() + " request is lost " + (count + 1) + " time(s)");

                // Receive and unmarshal the response.
                byte[] buffer = new byte[Serializer.BUF_SIZE];
                DatagramPacket res = new DatagramPacket(buffer, Serializer.BUF_SIZE);
                socket.receive(res);
                Response response = Response.Builder.parseFrom(buffer);
                if (response.getReqId() != request.getId()) {
                    logger.error(String.format("Error invoking %s: Request id mismatch", request.getName()));
                    return Optional.empty();
                }
                if (response.getStatus() == ResponseStatus.OK) {
                    return Optional.of(response.getValues()
                            .stream()
                            .map(Value::getVal)
                            .collect(Collectors.toList()));
                } else {
                    String errorMsg = response.getValues().size() > 0 ?
                            (String) response.getValues().get(0).getVal() : "";
                    logger.warn(String.format("Error invoking %s: response status %s: %s",
                            request.getName(),
                            response.getStatus(),
                            errorMsg));
                    return Optional.empty();
                }
            } catch (SocketTimeoutException e) {
                if (++count == maxRecvAttempts) {
                    logger.warn(String.format("No response received after %d attempts.", maxRecvAttempts));
                    throw e;
                }
            }
        }
    }
}
