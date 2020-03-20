package sg.edu.ntu.nfs.client;

import java.util.ArrayList;

public class OutRequestHandler {
    public OutRequestHandler(){}

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

    public int listDir(String dir){
        // TODO: request
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
