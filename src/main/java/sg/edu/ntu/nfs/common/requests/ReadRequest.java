package sg.edu.ntu.nfs.common.requests;

import sg.edu.ntu.nfs.common.values.Int32;
import sg.edu.ntu.nfs.common.values.Str;

public class ReadRequest extends AbstractRequest {
    public ReadRequest(RequestId id) {
        super(id, RequestType.READ, RequestType.READ.numParams());
    }

    public ReadRequest(String path, int offset, int count) {
        super(RequestType.READ, RequestType.READ.numParams());
        addParam(new Int32(offset));
        addParam(new Int32(count));
        addParam(new Str(path));
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
}
