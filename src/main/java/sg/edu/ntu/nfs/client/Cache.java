package sg.edu.ntu.nfs.client;

import java.util.HashMap;

public class Cache {
    private long freshnessInterval;
    private HashMap<String, CacheEntry> cachedFiles = new HashMap<>();

    public Cache(long freshnessInterval) {
        this.freshnessInterval = freshnessInterval;
    }

    /**
     * Check if a file is available in cache
     * @param filepath file path on server
     * @return true if the file is cached
     */
    public boolean exists(String filepath) {
        return cachedFiles.get(filepath) != null;
    }

    /**
     * Return file if cached, otherwise return null
     * @param filePath file path on server
     * @return cache entry of the requested file
     */
    public CacheEntry getFile(String filePath) {
        return cachedFiles.get(filePath);
    }

    /**
     * Add a new entry to the cached files
     * after getting a file from server
     * @param filePath    file path on server
     * @param fileContent content of the file
     * @param tMclient    last modification time on client - given by cache handler
     * @param tC          last validation time on client - given by cache handler
     */
    public void addFile(String filePath, byte[] fileContent, long tMclient, long tC) {
        CacheEntry newEntry = new CacheEntry(fileContent, tC, tMclient);
        cachedFiles.put(filePath, newEntry);
    }

    /**
     * Replace/update an existing cache entry
     * used when updating the server with a written file
     * @param filePath   file path on server
     * @param newContent new content of the file
     * @param tMclient   last modification time on client - given by cache handler
     * @param tC         last validation time on client - given by cache handler
     */
    public void replaceFile(String filePath, byte[] newContent, long tMclient, long tC) {
        CacheEntry entry = cachedFiles.get(filePath);
        entry.setFileContent(newContent);
        entry.setTmclient(tMclient);
        entry.setTc(tC);
        cachedFiles.replace(filePath, entry);
    }

    /**
     * Remove a cached file
     * @param filePath file path on server
     */
    public void removeFile(String filePath) {
        cachedFiles.remove(filePath);
    }

    public long getFreshnessInterval() {
        return this.freshnessInterval;
    }

    public void setFreshnessInterval(long val) {
        this.freshnessInterval = val;
    }
}
