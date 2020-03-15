package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Response;

public class ReadRequest extends Request {
    public ReadRequest(int id) {
        super(id, RequestName.READ, 3);
    }

    @Override
    public Response eval() {
        return null;
    }
}
