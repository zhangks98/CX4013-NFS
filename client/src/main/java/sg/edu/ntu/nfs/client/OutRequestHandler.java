package sg.edu.ntu.nfs.client;

import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.Response;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class OutRequestHandler {
    private static Proxy stub;
    public OutRequestHandler(){}

    public void set_proxy(InetAddress address, int port) throws SocketException {
        this.stub = new Proxy(address, port);
    }

    public byte[] requestFile(String file_path){
        //TODO: send request
        byte[] file_content = null;
        // TODO: extract file content
        return file_content;
    }

    public int updateFile(String file_path, byte[] new_content){
        // TODO: send request
        return 0;
    }

    public int requestTouch(String file_path){
        //TODO: request touch
        int response = 0;
        return response;
    }

    public int listDir(String dir) throws IOException {
        ListDirRequest req = new ListDirRequest(dir);
        Response res = stub.invoke(req);
        // TODO: handle the response
        return 0;
    }

    public int register(String file_path){
        // TODO: request
        return 0;
    }

    public long[] getattr(String file_path){
        long[] times = {0,0};
        return times;
    }
}
