package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.Response;

public class GetAttrRequest extends AbstractRequest {
    public GetAttrRequest(int id) {
        super(id, RequestName.GETATTR, 1);
    }

    @Override
    public Response eval() {
        return null;
    }
}
