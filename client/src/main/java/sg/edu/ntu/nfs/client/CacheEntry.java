package sg.edu.ntu.nfs.client;

public class CacheEntry {
    private byte[] file_content = null;
    private long t_c = 0;
    private long t_mclient = 0;

    /**
     * Entry in the client cache
     * @param file_content file content of the entry.
     * @param t_c last validation time.
     * @param t_mclient last modification time.
     */
    public CacheEntry(byte[] file_content, long t_c, long t_mclient){
        this.file_content = file_content;
        this.t_c = t_c;
        this.t_mclient = t_mclient;
    }

    public void setFileContent(byte[] new_content){
        this.file_content = new_content;
    }

    public byte[] getFileContent(){
        return this.file_content;
    }

    public void setTc(long new_tc){
        this.t_c = new_tc;
    }

    public long getTc(){
        return this.t_c;
    }

    public void setTmclient(long new_tmclient){
        this.t_mclient = new_tmclient;
    }

    public long getTmclient(){
        return this.t_mclient;
    }
}
