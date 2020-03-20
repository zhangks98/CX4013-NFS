package sg.edu.ntu.nfs.client;

import java.util.HashMap;

public class Cache {
    // freshness interval t
    private long fresh_t = -1;
    private HashMap<String, CacheEntry> cached_files = new HashMap<String, CacheEntry>();

    public Cache(){}

    /**
     * Return file if cached, otherwise return null
     * @param file_path file path on server
     */
    public CacheEntry getFile(String file_path){
        CacheEntry cached = cached_files.get(file_path);
        return cached;
    }

    /**
     * Add a new entry to the cached files
     * after getting a file from server
     * @param file_path file path on server
     * @param file_content content of the file
     * @param t_mclient last modification time on client - given by cache handler
     * @param t_c last validation time on client - given by cache handler
     */
    public void addFile(String file_path,  byte[] file_content, long t_mclient, long t_c){
        CacheEntry new_entry = new CacheEntry(file_content, t_c, t_mclient);
        cached_files.put(file_path, new_entry);
    }

    /**
     * Replace/update an existing cache entry
     * used when updating the server with a written file
     * @param file_path file path on server
     * @param new_content new content of the file
     * @param t_mclient last modification time on client - given by cache handler
     * @param t_c last validation time on client - given by cache handler
     */
    public void replaceFile(String file_path, byte[] new_content, long t_mclient, long t_c){
        CacheEntry entry = cached_files.get(file_path);
        entry.setFileContent(new_content);
        entry.setTmclient(t_mclient);
        entry.setTc(t_c);
        cached_files.replace(file_path, entry);
    }

    /**
     * Remove a cached file
     * @param file_path file path on server
     */
    public void removeFile(String file_path){
        cached_files.remove(file_path);
    }

    public void setFreshT(long val){
        this.fresh_t = val;
    }

    public long getFreshT(){
        return this.fresh_t;
    }
}
