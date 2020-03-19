package sg.edu.ntu.nfs.common.requests;

public enum RequestName {
    EMPTY(0),
    READ(3),
    WRITE(4),
    GETATTR(1);

    private final int numParams;

    RequestName(int numParams) {
        this.numParams = numParams;
    }

    public int numParams() {
        return numParams;
    }
}
