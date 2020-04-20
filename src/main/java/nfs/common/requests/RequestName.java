package nfs.common.requests;

public enum RequestName {
    EMPTY(0, EmptyRequest::new),
    READ(1, ReadRequest::new),
    INSERT(3, InsertRequest::new),
    GET_ATTR(1, GetAttrRequest::new),
    LIST_DIR(1, ListDirRequest::new),
    TOUCH(1, TouchRequest::new),
    REGISTER(2, RegisterRequest::new),
    APPEND(2, AppendRequest::new),
    FILE_UPDATED(3, FileUpdatedCallback::new);

    private final int numParams;
    private final RequestConstructor cls;

    RequestName(int numParams, RequestConstructor cls) {
        this.numParams = numParams;
        this.cls = cls;
    }

    public int numParams() {
        return numParams;
    }

    public AbstractRequest build(RequestId id) {
        return cls.get(id);
    }
}

/**
 * A functional interface to the constructor of a request type.
 */
interface RequestConstructor {
    /**
     * Construct a request with id.
     *
     * @param id the request id
     * @return the constructed request
     */
    AbstractRequest get(RequestId id);
}
