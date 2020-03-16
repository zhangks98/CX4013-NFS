package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Str;

public class GetAttrRequest extends AbstractRequest {
    public GetAttrRequest(RequestId id) {
        super(id, RequestName.GETATTR, RequestName.GETATTR.numParams());
    }

    public GetAttrRequest(String path) {
        super(RequestName.GETATTR, RequestName.GETATTR.numParams());
        addParam(new Str(path));
    }

    public String getPath() {
        return ((Str) getParam(0)).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }

}
