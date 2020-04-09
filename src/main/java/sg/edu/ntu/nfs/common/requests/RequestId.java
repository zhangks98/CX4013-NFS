package sg.edu.ntu.nfs.common.requests;

class RequestId {
    private static int nextId = 0;
    private final int id;

    RequestId() {
        this(nextId++);
    }

    RequestId(int id) {
        this.id = id;
    }

    static int getNextId() {
        return nextId;
    }

    int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RequestId))
            return false;
        return id == ((RequestId) obj).getId();
    }
}
