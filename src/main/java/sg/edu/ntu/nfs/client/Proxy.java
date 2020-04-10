package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.*;
import sg.edu.ntu.nfs.common.responses.*;
import sg.edu.ntu.nfs.common.values.Value;

import java.io.IOException;
import java.io.InvalidClassException;
import java.net.*;
import java.util.Optional;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class Proxy {
    private static final Logger logger = LogManager.getLogger();
    private final InetAddress address;
    private final int port;
    private DatagramSocket socket;
    private int timeout = 1000; // in milliseconds
    private int max_recv_attempts = 5;

    public Proxy(InetAddress address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    /**
     * Send a read request to the server
     * @param file_path file path on server
     * @return file content in bytes
     */
    public Optional<byte[]> requestFile(String file_path) throws IOException {
        Response res = invoke(new ReadRequest(file_path, 0, 0));
        Optional<byte[]> opt_content = Optional.empty();

        if (res.getStatus() == ResponseStatus.OK) {
            byte[] content = (byte[]) res.getValues().get(0).getVal();
            opt_content = Optional.of(content);
        } else {
            logger.warn("Error read: response status " + res.getStatus().toString());
        }
        return opt_content;
    }

    /**
     * Send a write request to the server
     * print the number of bytes written
     * @param file_path file path on server
     * @param offset offset of content insertion, measured in number of bytes
     * @param count number of bytes to write
     * @param data bytes to write
     */
    public void write(String file_path, int offset, int count, byte[] data) throws IOException {
        Response res = invoke(new WriteRequest(file_path, offset, count, data));
        if (res.getStatus() == ResponseStatus.OK) {
            int num_bytes_written = (int) res.getValues().get(0).getVal();
            System.out.println(num_bytes_written + " bytes written to " + file_path);
        } else {
            logger.warn("Error write: response status" + res.getStatus().toString());
        }
    }

    /**
     * Request to touch a file on the server
     * if file exists, last modified time of the file will be returned
     * otherwise, file will be created on the server
     * @param file_path file path on server
     */
    public void touch(String file_path) throws IOException {
        Response res = invoke(new TouchRequest(file_path));
        if (res.getStatus() == ResponseStatus.OK) {
            long atime = (long) res.getValues().get(0).getVal();
            System.out.println(file_path + "   Last accessed at: " + atime);
        } else {
            logger.warn("Error touch: response status " + res.getStatus().toString());
        }
    }

    /**
     * Send a request to list the contents of a directory on the server
     * @param dir directory of interest on server
     */
    public void listDir(String dir) throws IOException {
        Response res = invoke(new ListDirRequest(dir));
        if (res.getStatus() == ResponseStatus.OK) {
            for (Value val : res.getValues()) {
                String filename = (String) val.getVal();
                System.out.println(filename);
            }
        } else {
            logger.warn("Error listDir: response status " + res.getStatus().toString());
        }
    }

    /**
     * Send a request to register for updates of a file on server
     * @param file_path file path on server
     * @param monitor_interval duration for monitor file updates
     */
    public void register(String file_path, int monitor_interval) throws IOException {
        Response res = invoke(new RegisterRequest(file_path, monitor_interval));
        if (res.getStatus() == ResponseStatus.OK) {
            logger.info("Successfully registered");
        } else {
            logger.warn("Error register: response status " + res.getStatus().toString());
        }
    }

    /**
     * Request the last access time and last modified time of a file on the server
     * @param file_path file path on server
     * @return last modified time and last access time of the file
     */
    public Optional<long[]> getAttr(String file_path) throws IOException {
        Response res = invoke(new GetAttrRequest(file_path));
        Optional<long[]> opt_times = Optional.empty();

        if (res.getStatus() == ResponseStatus.OK){
            long mtime = (long) res.getValues().get(0).getVal();
            long atime = (long) res.getValues().get(1).getVal();
            long[] times = {mtime, atime};
            opt_times = Optional.of(times);
        } else {
            logger.warn("Error get attributes: response status " + res.getStatus().toString());
        }
        return opt_times;
    }

    /**
     * Invoke a remote procedure on the server and get response
     * If timeout, retry within maximum possible attempts
     * @param request client request
     * @return server response
     */
    private Response invoke(Request request) throws IOException {
        int count = 0;

        // Marshall and send the request.
        DatagramPacket req = new DatagramPacket(request.toBytes(), BUF_SIZE, address, port);

        while (true){
            try {
                socket.send(req);
                socket.setSoTimeout(timeout);

                // Receive and unmarshal the response.
                byte[] buffer = new byte[BUF_SIZE];
                DatagramPacket response = new DatagramPacket(buffer, BUF_SIZE);
                socket.receive(response);
                Response res = ResponseBuilder.parseFrom(buffer);
                return res;

            } catch (SocketTimeoutException e) {
                if ( ++count == max_recv_attempts) {
                    logger.warn(String.format("No response received after %d attempts.", max_recv_attempts));
                    throw e;
                }
            }
        }
    }
}