package sg.edu.ntu.nfs.server;

import java.net.InetAddress;

public class Context {
    private final InetAddress address;
    private final int port;

    public Context(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
