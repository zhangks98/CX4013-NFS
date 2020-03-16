package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Str;

public class GetAttrRequest extends GenericRequest {
    public GetAttrRequest(RequestId id) {
        super(id, RequestType.GETATTR, RequestType.GETATTR.numParams());
    }

    public GetAttrRequest(String path) {
        super(RequestType.GETATTR, RequestType.GETATTR.numParams());
        addParam(new Str(path));
    }

    public String getPath() {
        return ((Str) getParam(0)).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }

}
