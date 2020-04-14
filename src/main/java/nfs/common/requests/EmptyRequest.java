package nfs.common.requests;

/**
 * An empty request with no specific purpose.
 */
public class EmptyRequest extends AbstractRequest {
    EmptyRequest(RequestId id) {
        super(id, RequestName.EMPTY);
    }

    public EmptyRequest() {
        super(RequestName.EMPTY);
    }
}
