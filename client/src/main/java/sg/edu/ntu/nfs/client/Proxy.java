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
        try{
            Response res = invoke(new ReadRequest(file_path, 0, 0));
            if(res.getStatus() == ResponseStatus.OK){
                byte[] content = (byte[]) res.getValues().get(0).getVal();
                return content;
            }
        }catch (IOException ex){
            logger.warn("Error read", ex);
        }
        return null;
    }

    public void write(String file_path, int offset, int count, byte[] data) {
        try{
            Response res = invoke(new WriteRequest(file_path, offset, count, data));
            if (res.getStatus() == ResponseStatus.OK){
                int num_bytes_written = (int) res.getValues().get(0).getVal();
                System.out.println(num_bytes_written + " bytes written to " + file_path);
            }else{
                System.out.println("Error write");
            }
        }catch (IOException ex){
            logger.warn("Error write", ex);
        }
    }

    public void touch(String file_path) {
        try{
            Response res = invoke(new TouchRequest(file_path));
            if (res.getStatus() == ResponseStatus.OK) {
                long atime = (long) res.getValues().get(0).getVal();
                System.out.println(file_path + "   Last accessed at: " + atime);
            }else{
                System.out.println("Error touch");
            }
        }catch (IOException ex){
            logger.warn("Error touch", ex);
        }
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

    public void register(String file_path, int monitor_interval) {
        try{
            Response res = invoke(new RegisterRequest(file_path, monitor_interval));
            if (res.getStatus() == ResponseStatus.OK){
                System.out.println("Successfully registered");
            }else{
                System.out.println("Error register");
            }
        }catch (IOException ex){
            logger.warn("Error register", ex);
        }
    }

    public long[] getattr(String file_path) {
        try{
            Response res = invoke(new GetAttrRequest(file_path));
            if (res.getStatus() == ResponseStatus.OK){
                long mtime = (long) res.getValues().get(0).getVal();
                long atime = (long) res.getValues().get(1).getVal();
                long[] times = {mtime, atime};
                return times;
            }
        }catch (IOException ex){
            logger.warn("Error get attributes", ex);
        }
        long[] times = {-1, -1};
        return times;
    }

    private Response invoke(Request request) throws IOException {
        // Marshall and send the request.

        DatagramPacket req = new DatagramPacket(request.toBytes(), BUF_SIZE, address, port);
        socket.send(req);

        byte[] buffer = new byte[BUF_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, BUF_SIZE);
        socket.receive(response);

        return ResponseBuilder.parseFrom(buffer);
    }
}
