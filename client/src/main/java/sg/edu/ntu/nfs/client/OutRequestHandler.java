package sg.edu.ntu.nfs.client;

import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.responses.Response;
import sg.edu.ntu.nfs.common.responses.ResponseStatus;
import sg.edu.ntu.nfs.common.values.Value;

import java.io.IOException;

public class OutRequestHandler {
    private final Proxy stub;

    public OutRequestHandler(Proxy stub){
        this.stub = stub;
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

    public void listDir(String dir) throws IOException {
        Response res = stub.invoke(new ListDirRequest(dir));
        if (res.getStatus() == ResponseStatus.OK) {
            for (Value val : res.getValues()) {
                String filename = (String) val.getVal();
                System.out.println(filename);
            }
        }
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
