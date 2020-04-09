package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Int32;
import sg.edu.ntu.nfs.common.values.Str;

public class ReadRequest extends AbstractRequest {
    ReadRequest(RequestId id) {
        super(id, RequestName.READ);
    }

    /**
     * Read the file from specified path starting at offset and read for count bytes
     * @param path the file path.
     * @param offset where to start reading.
     * @param count the number of bytes to read.
     */
    public ReadRequest(String path, int offset, int count) {
        super(RequestName.READ);
        addParam(new Int32(offset));
        addParam(new Int32(count));
        addParam(new Str(path));
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
}
