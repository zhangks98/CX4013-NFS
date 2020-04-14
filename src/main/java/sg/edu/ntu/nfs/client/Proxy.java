package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.*;
import sg.edu.ntu.nfs.common.responses.Response;
import sg.edu.ntu.nfs.common.responses.ResponseBuilder;
import sg.edu.ntu.nfs.common.responses.ResponseStatus;
import sg.edu.ntu.nfs.common.values.Value;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class Proxy {
    private static final Logger logger = LogManager.getLogger();
    private final InetAddress address;
    private final int port;
    private final DatagramSocket socket;
    private final int timeout = 500; // in milliseconds
    private final int maxRecvAttempts = 5;

    public Proxy(InetAddress address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    /**
     * Send a read request to the server
     * <p>
     * <<<<<<< HEAD
     *
     * @param filePath file path on server
     * @return file content in bytes
     */
    public Optional<byte[]> requestFile(String filePath) throws IOException {
        return invoke(new ReadRequest(filePath)).map(res -> (byte[]) res.get(0));
    }

    /**
     * Send a write request to the server
     * print the number of bytes written
     *
     * @param filePath file path on server
     * @param offset   offset of content insertion, measured in number of bytes
     * @param data     bytes to write
     */
    public void write(String filePath, int offset, byte[] data) throws IOException {
        invoke(new WriteRequest(filePath, offset, data))
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
            System.out.println(filePath + " Last accessed at: " + atime);
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
     * Send a request to register for updates of a file on server
     *
     * @param filePath         file path on server
     * @param monitor_interval duration for monitor file updates
     */
    public void register(String filePath, int monitor_interval) throws IOException {
        invoke(new RegisterRequest(filePath, monitor_interval))
                .ifPresent(res -> logger.info("Successfully registered"));
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
     * Invoke a remote procedure on the server and get response
     * If timeout, retry within maximum possible attempts
     *
     * @param request client request
     * @return server response
     */
    private Optional<List<Object>> invoke(Request request) throws IOException {
        int count = 0;

        // Marshall and send the request.
        DatagramPacket req = new DatagramPacket(request.toBytes(), BUF_SIZE, address, port);

        while (true) {
            try {
                socket.send(req);
                socket.setSoTimeout(timeout);

                // Receive and unmarshal the response.
                byte[] buffer = new byte[BUF_SIZE];
                DatagramPacket res = new DatagramPacket(buffer, BUF_SIZE);
                socket.receive(res);
                Response response = ResponseBuilder.parseFrom(buffer);
                if (response.getStatus() == ResponseStatus.OK) {
                    return Optional.of(response.getValues()
                            .stream()
                            .map(Value::getVal)
                            .collect(Collectors.toList()));
                } else {
                    if (response.getValues().size() > 0) {
                        logger.warn(String.format("Error invoking %s: response status %s: %s",
                                request.getName(),
                                response.getStatus(),
                                response.getValues().get(0).getVal()));
                    } else {
                        logger.warn(String.format("Error invoking %s: response status %s",
                                request.getName(),
                                response.getStatus()));
                    }
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
