package sg.edu.ntu.nfs.server;

import java.io.File;

public class AtMostOnceServicer extends AtLeastOnceServicer {

    public AtMostOnceServicer(File rootDir) {
        super(rootDir);
    }

}
