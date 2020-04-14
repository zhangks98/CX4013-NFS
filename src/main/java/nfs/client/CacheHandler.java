package nfs.client;

import java.io.IOException;
import java.util.Optional;

public class CacheHandler {

    static Cache cache; // only one instance
    private final Proxy stub;

    public CacheHandler(Proxy stub, long freshInterval) {
        this.stub = stub;
        cache = new Cache(freshInterval);
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
                byte[] fileContent = optContent.get();
                long tMclient = System.currentTimeMillis();
                long tC = System.currentTimeMillis();
                cache.addFile(filePath, fileContent, tMclient, tC);
            }
        } else {
            CacheEntry entry = cache.getFile(filePath);
            System.out.println((entry != null));
            // check freshness upon access
            if (System.currentTimeMillis() - entry.getTc() >= cache.getFreshnessInterval()) {
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

    public Cache getCache() {
        return cache;
    }

}
