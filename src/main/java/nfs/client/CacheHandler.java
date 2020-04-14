package nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class CacheHandler {
    private static final Logger logger = LogManager.getLogger();
    static Cache cache; // only one instance
    private final Proxy stub;
    private final long freshInterval;

    public CacheHandler(Proxy stub, long freshInterval) {
        this.stub = stub;
        this.freshInterval = freshInterval;
        cache = new Cache();
    }

    /**
     * Get file from cache
     * if file not in cache, request file and cache it
     * validate the file upon access
     *
     * @param filePath file path on server
     * @return file content in bytes
     */
    public Optional<byte[]> getFile(String filePath) throws IOException {
        Optional<byte[]> optContent;

        // not in cache
        if (!cache.exists(filePath)) {
            optContent = stub.requestFile(filePath);
            // file found on server
            if (optContent.isPresent()) {
                logger.info(filePath + " is not cached. File retrieved from server.");
                byte[] fileContent = optContent.get();
                long tMclient = System.currentTimeMillis();
                long tC = System.currentTimeMillis();
                cache.addFile(filePath, fileContent, tMclient, tC);
            }
        } else {
            CacheEntry entry = cache.getFile(filePath);
            logger.info(filePath + " retrieved from cache");
            // check freshness upon access
            if (System.currentTimeMillis() - entry.getTc() >= freshInterval) {
                Optional<long[]> optAttr = stub.getAttr((filePath));

                if (optAttr.isPresent()) {
                    long[] attr = optAttr.get();
                    long tMserver = attr[0];

                    // invalid entry
                    if (entry.getTmclient() < tMserver) {
                        // update entry
                        Optional<byte[]> optFile = stub.requestFile(filePath);

                        if (optFile.isPresent()) {
                            byte[] fileContent = optFile.get();

                            // file still on server
                            long tMclient = System.currentTimeMillis();
                            long tC = System.currentTimeMillis();
                            cache.replaceFile(filePath, fileContent, tMclient, tC);
                        } else {
                            // file no longer available on server
                            cache.removeFile(filePath);
                        }
                    }
                }
            }
            optContent = Optional.of(entry.getFileContent());
        }
        return optContent;
    }

    /**
     * Update a file on client: if cached, replace the old file, else add file
     *
     * @param filePath   file path on server
     * @param newContent updated content of the file
     */
    public void updateFile(String filePath, byte[] newContent) {
        long now = System.currentTimeMillis();
        if (cache.exists(filePath)) {
            cache.replaceFile(filePath, newContent, now, now);
        } else {
            cache.addFile(filePath, newContent, now, now);
        }
    }

    /**
     * Send a write request to the server
     * Remove the file from cache (if cached) to maintain cache consistency
     *
     * @param filePath file path on the server
     * @param offset   offset in bytes
     * @param data     data in bytes
     * @throws IOException
     */
    public void writeFile(String filePath, int offset, byte[] data) throws IOException {
        stub.write(filePath, offset, data);
        removeFromCache(filePath);
    }

    /**
     * Remove a cached file
     *
     * @param filePath file path on the server
     */
    public void removeFromCache(String filePath) {
        if (cache.exists(filePath))
            cache.removeFile(filePath);
    }

    public Cache getCache() {
        return cache;
    }

}
