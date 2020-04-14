package nfs.common.requests;

import nfs.common.values.Str;

public class GetAttrRequest extends AbstractRequest {
    GetAttrRequest(RequestId id) {
        super(id, RequestName.GET_ATTR);
    }

    /**
     * Get the status of the file, including the access time and modified time.
     *
     * @param path the file path.
     */
    public GetAttrRequest(String path) {
        super(RequestName.GET_ATTR);
        addParam(new Str(path));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }

}
