package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg.edu.ntu.nfs.common.requests.*;
import sg.edu.ntu.nfs.common.responses.*;
import sg.edu.ntu.nfs.common.values.Value;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class Proxy {
    private static final Logger logger = LogManager.getLogger();
    private final InetAddress address;
    private final int port;
    private DatagramSocket socket;

    public Proxy(InetAddress address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public byte[] requestFile(String file_path) {
        //TODO: send request
        byte[] file_content = null;
        // TODO: extract file content
        return file_content;
    }

    public int updateFile(String file_path, byte[] new_content) {
        // TODO: send request
        return 0;
    }

    public int requestTouch(String file_path) {
        //TODO: request touch
        int response = 0;
        return response;
    }


    public void listDir(String dir) {
        try {
            Response res = invoke(new ListDirRequest(dir));
            if (res.getStatus() == ResponseStatus.OK) {
                for (Value val : res.getValues()) {
                    String filename = (String) val.getVal();
                    System.out.println(filename);
                }
            }
        } catch (IOException ex) {
            logger.warn("Error listDir", ex);
        }
    }

    public int register(String file_path) {
        // TODO: request
        return 0;
    }

    public long[] getattr(String file_path) {
        long[] times = {0, 0};
        return times;
    }

    private Response invoke(Request request) throws IOException {
        // Marshall and send the request.
        DatagramPacket req = new DatagramPacket(request.toBytes(), BUF_SIZE, address, port);
        socket.send(req);

        // Receive and unmarshal the response.
        byte[] buffer = new byte[BUF_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, BUF_SIZE);
        socket.receive(response);

        return ResponseBuilder.parseFrom(buffer);
    }
}
