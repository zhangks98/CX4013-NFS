package nfs.common.requests;

import nfs.common.values.Str;

public class TouchRequest extends AbstractRequest {
    TouchRequest(RequestId id) {
        super(id, RequestName.TOUCH);
    }

    /**
     * Create a new file or change its access time to now.
     *
     * @param path the file path.
     */
    public TouchRequest(String path) {
        super(RequestName.TOUCH);
        addParam(new Str(path));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }
}
