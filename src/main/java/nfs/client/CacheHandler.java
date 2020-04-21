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
        Optional<long[]> optAttr = stub.getAttr((filePath));

        // not in cache
        if (!cache.exists(filePath)) {
            Optional<byte[]> optContent = stub.requestFile(filePath);

            // file found on server
            if (optContent.isPresent() && optAttr.isPresent()) {
                logger.info(filePath + " is not cached. File retrieved from server.");
                byte[] fileContent = optContent.get();
                long tMserver = optAttr.get()[0];
                long now = System.currentTimeMillis();
                cache.putFile(filePath, fileContent, tMserver, now);
            }
            return optContent;
        }

        // File is in the cache
        CacheEntry entry = cache.getFile(filePath);
        // check freshness upon access
        if (System.currentTimeMillis() - entry.getTc() >= freshInterval) {
            logger.info("Cached copy of " + filePath +
                    " has exceeded the freshness interval. Validating...");

            if (optAttr.isEmpty()) {
                cache.removeFile(filePath);
                logger.warn(filePath + " removed from cache due to invalid file attribute.");
                return Optional.empty();
            } else {
                long tMserver = optAttr.get()[0];
                // invalid entry
                if (entry.getTmclient() < tMserver) {
                    logger.info("Invalid cached copy of " + filePath +
                            " Requesting the latest copy from server...");
                    Optional<byte[]> optFile = stub.requestFile(filePath);

                    // file still on server
                    if (optFile.isPresent()) {
                        byte[] fileContent = optFile.get();
                        long now = System.currentTimeMillis();
                        cache.putFile(filePath, fileContent, tMserver, now);
                        logger.info("Updated the cached copy of " + filePath);
                    } else {
                        cache.removeFile(filePath);
                        logger.warn(filePath + " removed from cache due to read error.");
                    }
                    return optFile;

                } else if (entry.getTmclient() > tMserver) {
                    logger.error("Invalid file attribute: Tmclient > Tmserver");
                    cache.removeFile(filePath);
                    logger.warn(filePath + " removed from cache due to invalid file attribute.");
                    return Optional.empty();
                } else {
                    // File has not been modified on server. Set Tc to be now.
                    logger.info("File has not been modified on server. Updating Tc...");
                    long now = System.currentTimeMillis();
                    // In principle, this is a not thread-safe operation. But cache entries are not shared with
                    // other threads (update put a new instance of cache entry, so here it is safe to do this.
                    entry.setTc(now);
                }
            }
        }

        // The entry is within freshness interval or has not been modified on the server.
        logger.info("Retrieved " + filePath + " from cache.");
        return Optional.of(entry.getFileContent());
    }

    /**
     * Update a file on client: if cached, replace the old file, else add file
     *
     * @param filePath   file path on server.
     * @param tMserver   last modification time on server
     * @param newContent updated content of the file
     */
    public void updateFile(String filePath, long tMserver, byte[] newContent) {
        long now = System.currentTimeMillis();
        cache.putFile(filePath, newContent, tMserver, now);
    }

    /**
     * Send an insert request to the server
     * Remove the file from cache (if cached) to maintain cache consistency
     *
     * @param filePath file path on the server
     * @param offset   offset in bytes
     * @param data     data in bytes
     * @throws IOException
     */
    public void insertFile(String filePath, int offset, byte[] data) throws IOException {
        if (cache.exists(filePath))
            cache.removeFile(filePath);
        stub.insert(filePath, offset, data);
    }

    /**
     * Send an append request to the server
     * Remove the file from cache (if cached) to maintain cache consistency
     *
     * @param filePath file path on the server
     * @param data     data in bytes
     * @throws IOException
     */
    public void appendFile(String filePath, byte[] data) throws IOException {
        if (cache.exists(filePath))
            cache.removeFile(filePath);
        stub.append(filePath, data);
    }

    public Cache getCache() {
        return cache;
    }

}
