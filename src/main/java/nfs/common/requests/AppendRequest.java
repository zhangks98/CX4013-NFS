package nfs.common.requests;

import nfs.common.values.Bytes;
import nfs.common.values.Str;

public class AppendRequest extends AbstractRequest {
    AppendRequest(RequestId id) {
        super(id, RequestName.APPEND);
    }

    /**
     * The callback from server to client to update the file.
     *
     * @param path the path of the updated file.
     * @param data the updated file content.
     */
    public AppendRequest(String path, byte[] data) {
        super(RequestName.APPEND);
        addParam(new Str(path));
        addParam(new Bytes(data));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }

    public byte[] getData() {
        return (byte[]) getParam(1).getVal();
    }

    public void setData(byte[] data) {
        setParam(1, new Bytes(data));
    }
}
