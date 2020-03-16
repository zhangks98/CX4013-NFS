package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Bytes;
import sg.edu.ntu.nfs.common.values.Int32;
import sg.edu.ntu.nfs.common.values.Str;

public class WriteRequest extends GenericRequest {
    public WriteRequest(RequestId id) {
        super(id, RequestName.WRITE, RequestName.WRITE.numParams());
    }

    public WriteRequest(String path, int offset, int count, byte[] data) {
        super(RequestName.WRITE, RequestName.WRITE.numParams());
        addParam(new Int32(offset));
        addParam(new Int32(count));
        addParam(new Str(path));
        addParam(new Bytes(data));
    }

    public String getPath() {
        return ((Str) getParam(2)).getVal();
    }

    public void setPath(String path) {
        setParam(2, new Str(path));
    }

    public int getOffset() {
        return ((Int32) getParam(0)).getVal();
    }

    public void setOffset(int offset) {
        setParam(0, new Int32(offset));
    }

    public int getCount() {
        return ((Int32) getParam(1)).getVal();
    }

    public void setCount(int count) {
        setParam(1, new Int32(count));
    }

    public byte[] getData() {
        return ((Bytes) getParam(3)).getVal();
    }

    public void setData(byte[] data) {
        setParam(3, new Bytes(data));
    }
}
