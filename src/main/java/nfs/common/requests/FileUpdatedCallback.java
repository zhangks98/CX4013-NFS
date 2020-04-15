package nfs.common.requests;

import nfs.common.values.Bytes;
import nfs.common.values.Int64;
import nfs.common.values.Str;

public class FileUpdatedCallback extends AbstractRequest {
    FileUpdatedCallback(RequestId id) {
        super(id, RequestName.FILE_UPDATED);
    }

    /**
     * The callback from server to client to update the file.
     *
     * @param path the path of the updated file.
     * @param data the updated file content.
     */
    public FileUpdatedCallback(String path, long mtime, byte[] data) {
        super(RequestName.FILE_UPDATED);
        addParam(new Str(path));
        addParam(new Int64(mtime));
        addParam(new Bytes(data));
    }

    public String getPath() {
        return (String) getParam(0).getVal();
    }

    public void setPath(String path) {
        setParam(0, new Str(path));
    }

    public long getMtime() {
        return (long) getParam(1).getVal();
    }

    public void setMtime(long mtime) {
        setParam(1, new Int64(mtime));
    }

    public byte[] getData() {
        return (byte[]) getParam(2).getVal();
    }

    public void setData(byte[] data) {
        setParam(2, new Bytes(data));
    }
}
