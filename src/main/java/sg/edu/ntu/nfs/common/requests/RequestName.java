package sg.edu.ntu.nfs.common.requests;

public enum RequestName {
    EMPTY(0),
    READ(1),
    WRITE(3),
    GET_ATTR(1),
    LIST_DIR(1),
    TOUCH(1),
    REGISTER(2),
    FILE_UPDATED(2);

    private final int numParams;

    RequestName(int numParams) {
        this.numParams = numParams;
    }

    public int numParams() {
        return numParams;
    }
}
