package sg.edu.ntu.nfs.client;

public class CacheEntry {
    private byte[] fileContent = null;
    private long tC = 0;
    private long tMclient = 0;

    /**
     * Entry in the client cache
     * @param fileContent file content of the entry.
     * @param tC          last validation time.
     * @param tMclient    last modification time.
     */
    public CacheEntry(byte[] fileContent, long tC, long tMclient) {
        this.fileContent = fileContent;
        this.tC = tC;
        this.tMclient = tMclient;
    }

    public byte[] getFileContent() {
        return this.fileContent;
    }

    public void setFileContent(byte[] newContent) {
        this.fileContent = newContent;
    }

    public long getTc() {
        return this.tC;
    }

    public void setTc(long newTc) {
        this.tC = newTc;
    }

    public long getTmclient() {
        return this.tMclient;
    }

    public void setTmclient(long newTmclient) {
        this.tMclient = newTmclient;
    }
}
