package sg.edu.ntu.nfs.client;

import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.Response;
import sg.edu.ntu.nfs.common.responses.ResponseBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static sg.edu.ntu.nfs.common.Serializer.BUF_SIZE;

public class Proxy {
    private final InetAddress address;
    private final int port;
    private DatagramSocket socket;

    /**
     * Construct the client-side proxy for RPC.
     *
     * @param address the server address.
     * @param port the server port.
     * @throws SocketException
     */
    public Proxy(InetAddress address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public Response invoke(Request request) throws IOException {
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
