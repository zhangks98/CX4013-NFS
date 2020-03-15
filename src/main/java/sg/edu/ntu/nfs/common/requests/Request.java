package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Response;
import sg.edu.ntu.nfs.common.Serializer;

public interface Request extends Serializer {
    Response eval();
    int getNumParams();
}
