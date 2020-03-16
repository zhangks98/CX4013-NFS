package sg.edu.ntu.nfs.common.requests;

public class RequestId {
    private static int nextId = 0;
    private final int id;

    public RequestId() {
        this(nextId++);
    }

    public RequestId(int id) {
        this.id = id;
    }

    public static int getNextId() {
        return nextId;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RequestId))
            return false;
        return id == ((RequestId) obj).getId();
    }
}
