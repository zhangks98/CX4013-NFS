package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Str;

public class ListDirRequest extends AbstractRequest {
    ListDirRequest(RequestId id) {
        super(id, RequestName.LIST_DIR);
    }

    /**
     * List all files in the specified directory
     * @param path the directory path.
     */
    public ListDirRequest(String path) {
        super(RequestName.LIST_DIR);
        addParam(new Str(path));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }
}
