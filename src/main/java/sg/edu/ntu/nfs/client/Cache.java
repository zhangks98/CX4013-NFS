package sg.edu.ntu.nfs.client;

import java.util.HashMap;

public class Cache {
    private long freshness_interval;
    private HashMap<String, CacheEntry> cached_files = new HashMap<String, CacheEntry>();

    public Cache(long freshness_interval) {
        this.freshness_interval = freshness_interval;
    }

    /**
     * Check if a file is available in cache
     * @param filepath file path on server
     * @return true if the file is cached
     */
    public boolean exists(String filepath) {
        return cached_files.get(filepath) != null;
    }

    /**
     * Return file if cached, otherwise return null
     * @param filePath file path on server
     * @return cache entry of the requested file
     */
    public CacheEntry getFile(String filePath) {
        return cached_files.get(filePath);
    }

    /**
     * Add a new entry to the cached files
     * after getting a file from server
     * @param filePath    file path on server
     * @param file_content content of the file
     * @param t_mclient    last modification time on client - given by cache handler
     * @param t_c          last validation time on client - given by cache handler
     */
    public void addFile(String filePath, byte[] file_content, long t_mclient, long t_c) {
        CacheEntry new_entry = new CacheEntry(file_content, t_c, t_mclient);
        cached_files.put(filePath, new_entry);
    }

    /**
     * Replace/update an existing cache entry
     * used when updating the server with a written file
     * @param filePath   file path on server
     * @param new_content new content of the file
     * @param t_mclient   last modification time on client - given by cache handler
     * @param t_c         last validation time on client - given by cache handler
     */
    public void replaceFile(String filePath, byte[] new_content, long t_mclient, long t_c) {
        CacheEntry entry = cached_files.get(filePath);
        entry.setFileContent(new_content);
        entry.setTmclient(t_mclient);
        entry.setTc(t_c);
        cached_files.replace(filePath, entry);
    }

    /**
     * Remove a cached file
     * @param filePath file path on server
     */
    public void removeFile(String filePath) {
        cached_files.remove(filePath);
    }

    public long getFreshT() {
        return this.freshness_interval;
    }

    public void setFreshT(long val) {
        this.freshness_interval = val;
    }
}
