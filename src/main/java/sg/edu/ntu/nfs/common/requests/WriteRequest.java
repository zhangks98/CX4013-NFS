package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Bytes;
import sg.edu.ntu.nfs.common.values.Int32;
import sg.edu.ntu.nfs.common.values.Str;

public class WriteRequest extends AbstractRequest {
    public WriteRequest(RequestId id) {
        super(id, RequestName.WRITE);
    }

    /**
     * Write count number of bytes to file starting at offset.
     * @param path the file path.
     * @param offset the starting point to write data.
     * @param count the number of bytes to write.
     * @param data the data to write.
     */
    public WriteRequest(String path, int offset, int count, byte[] data) {
        super(RequestName.WRITE);
        addParam(new Int32(offset));
        addParam(new Int32(count));
        addParam(new Str(path));
        addParam(new Bytes(data));
    }

    public String getPath() {
        return (String) getParam(2).getVal();
    }

    public void setPath(String path) {
        setParam(2, new Str(path));
    }

    public int getOffset() {
        return (int) getParam(0).getVal();
    }

    public void setOffset(int offset) {
        setParam(0, new Int32(offset));
    }

    public int getCount() {
        return (int) getParam(1).getVal();
    }

    public void setCount(int count) {
        setParam(1, new Int32(count));
    }

    public byte[] getData() {
        return (byte[]) getParam(3).getVal();
    }

    public void setData(byte[] data) {
        setParam(3, new Bytes(data));
    }
}
