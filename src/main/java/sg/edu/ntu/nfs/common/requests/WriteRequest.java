package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Response;

public class WriteRequest extends AbstractRequest {
    public WriteRequest(int id) {
        super(id, RequestName.WRITE, 3);
    }

    @Override
    public Response eval() {
        return null;
    }
}
