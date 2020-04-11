package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Str;

public class ReadRequest extends AbstractRequest {
    ReadRequest(RequestId id) {
        super(id, RequestName.READ);
    }

    /**
     * Read the file from specified path starting at offset and read for count bytes
     * @param path the file path.
     *
     */
    public ReadRequest(String path) {
        super(RequestName.READ);
        addParam(new Str(path));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }
}
