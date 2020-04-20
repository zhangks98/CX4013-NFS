package nfs.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private final Map<String, CacheEntry> cachedFiles = new ConcurrentHashMap<>();

    /**
     * Check if a file is available in cache
     *
     * @param filepath file path on server
     * @return true if the file is cached
     */
    public boolean exists(String filepath) {
        return cachedFiles.get(filepath) != null;
    }

    /**
     * Return file if cached, otherwise return null
     *
     * @param filePath file path on server
     * @return cache entry of the requested file
     */
    public CacheEntry getFile(String filePath) {
        return cachedFiles.get(filePath);
    }

    /**
     * Put an entry to the cached files
     * If the map previously contained a mapping for the filePath,
     * the old value is replaced.
     *
     * @param filePath    file path on server
     * @param fileContent content of the file
     * @param tMclient    last modification time on client - given by cache handler
     * @param tC          last validation time on client - given by cache handler
     */
    public void putFile(String filePath, byte[] fileContent, long tMclient, long tC) {
        CacheEntry newEntry = new CacheEntry(fileContent, tC, tMclient);
        cachedFiles.put(filePath, newEntry);
    }

    /**
     * Put a cache entry to the cached files
     * If the map previously contained a mapping for the filePath,
     * the old value is replaced.
     *
     * @param filePath file path on server
     * @param entry    the cache entry
     */
    public void putEntry(String filePath, CacheEntry entry) {
        cachedFiles.put(filePath, entry);
    }

    /**
     * Remove a cached file
     *
     * @param filePath file path on server
     */
    public void removeFile(String filePath) {
        cachedFiles.remove(filePath);
    }

}
