package sg.edu.ntu.nfs.common.requests;

public class EmptyRequest extends AbstractRequest {
    public EmptyRequest(RequestId id) {
        super(id, RequestName.EMPTY, RequestName.EMPTY.numParams());
    }

    public EmptyRequest() {
        super(RequestName.EMPTY, RequestName.EMPTY.numParams());
    }
}
