package sg.edu.ntu.nfs.common.requests;

public class EmptyRequest extends AbstractRequest {
    public EmptyRequest(RequestId id) {
        super(id, RequestType.EMPTY, RequestType.EMPTY.numParams());
    }

    public EmptyRequest() {
        super(RequestType.EMPTY, RequestType.EMPTY.numParams());
    }
}
