package nfs.common.requests;

import nfs.common.values.Bytes;
import nfs.common.values.Int32;
import nfs.common.values.Str;

public class WriteRequest extends AbstractRequest {
    public WriteRequest(RequestId id) {
        super(id, RequestName.WRITE);
    }

    /**
     * Write count number of bytes to file starting at offset.
     *
     * @param path   the file path.
     * @param offset the starting point to write data.
     * @param data   the data to write.
     */
    public WriteRequest(String path, int offset, byte[] data) {
        super(RequestName.WRITE);
        addParam(new Int32(offset));
        addParam(new Str(path));
        addParam(new Bytes(data));
    }

    public String getPath() {
        return (String) getParam(1).getVal();
    }

    public void setPath(String path) {
        setParam(1, new Str(path));
    }

    public int getOffset() {
        return (int) getParam(0).getVal();
    }

    public void setOffset(int count) {
        setParam(0, new Int32(count));
    }

    public byte[] getData() {
        return (byte[]) getParam(2).getVal();
    }

    public void setData(byte[] data) {
        setParam(2, new Bytes(data));
    }
}
