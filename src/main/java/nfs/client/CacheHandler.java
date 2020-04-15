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
            Optional<long[]> optAttr = stub.getAttr((filePath));

            // file found on server
            if (optContent.isPresent() && optAttr.isPresent()) {
                logger.info(filePath + " is not cached. File retrieved from server.");
                byte[] fileContent = optContent.get();
                long tMserver = optAttr.get()[0];
                long now = System.currentTimeMillis();
                cache.addFile(filePath, fileContent, tMserver, now);
            }

        } else {
            CacheEntry entry = cache.getFile(filePath);

            // check freshness upon access
            // logger.info("Time lapsed: " + (System.currentTimeMillis() - entry.getTc()));
            if (System.currentTimeMillis() - entry.getTc() >= freshInterval) {
                logger.info("Last validation for cached copy of " + filePath +
                        " has exceeded the freshness interval. Validating...");

                Optional<long[]> optAttr = stub.getAttr((filePath));

                if (optAttr.isPresent()) {
                    long[] attr = optAttr.get();
                    long tMserver = attr[0];

                    // invalid entry
                    if (entry.getTmclient() < tMserver) {
                        logger.info("The current cached copy of " + filePath + " is invalid.");
                        logger.info("Requesting latest copy from server...");
                        Optional<byte[]> optFile = stub.requestFile(filePath);

                        // file still on server
                        if (optFile.isPresent()) {
                            byte[] fileContent = optFile.get();
                            long now = System.currentTimeMillis();
                            cache.replaceFile(filePath, fileContent, tMserver, now);
                            logger.info("Updated the cached copy of " + filePath + " with its latest copy on server.");

                        } else {
                            cache.removeFile(filePath);
                            logger.info(filePath + " removed from cache since it has been removed from server.");
                        }
                    }
                }
            } else
                logger.info("Retrieved " + filePath + " from cache.");

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
