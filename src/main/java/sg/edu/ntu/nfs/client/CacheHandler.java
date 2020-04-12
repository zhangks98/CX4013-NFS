package sg.edu.ntu.nfs.client;

import java.io.IOException;
import java.util.Optional;

public class CacheHandler {

    static Cache cache; // only one instance
    private final Proxy stub;

    public CacheHandler(Proxy stub, long fresh_interval) {
        this.stub = stub;
        cache = new Cache(fresh_interval);
    }

    /**
     * Get file from cache
     * if file not in cache, request file and cache it
     * validate the file upon access
     * @param filePath file path on server
     * @return file content in bytes
     */
    public Optional<byte[]> getFile(String filePath) throws IOException {
        Optional<byte[]> opt_content;

        // not in cache
        if (cache.exists(filePath)) {
            opt_content = stub.requestFile(filePath);
            // file found on server
            if (opt_content.isPresent()) {
                byte[] file_content = opt_content.get();
                long t_mclient = System.currentTimeMillis();
                long t_c = System.currentTimeMillis();
                cache.addFile(filePath, file_content, t_mclient, t_c);
            }
        }
        else {
            CacheEntry entry = cache.getFile(filePath);
            // check freshness upon access
            if (System.currentTimeMillis() - entry.getTc() >= cache.getFreshT()) {
                Optional<long[]> opt_attr = stub.getAttr((filePath));

                if (opt_attr.isPresent()) {
                    long[] attr = opt_attr.get();
                    long t_mserver = attr[0];

                    // invalid entry
                    if (entry.getTmclient() < t_mserver) {
                        // update entry
                        Optional<byte[]> opt_file = stub.requestFile(filePath);

                        if (opt_file.isPresent()) {
                            byte[] file_content = opt_file.get();

                            // file still on server
                            if (file_content != null) {
                                long t_mclient = System.currentTimeMillis();
                                long t_c = System.currentTimeMillis();
                                cache.replaceFile(filePath, file_content, t_mclient, t_c);
                            }
                            // file no longer available on server
                            else
                                cache.removeFile(filePath);
                        }
                    }
                }
            }
            opt_content = Optional.of(entry.getFileContent());
        }
        return opt_content;
    }

    /**
     * Update a file on client: if cached, replace the old file, else add file
     * @param filePath file path on server
     * @param new_content updated content of the file
     */
    public void updateFile(String filePath, byte[] new_content) {
        long now = System.currentTimeMillis();
        if (cache.exists(filePath)) {
            cache.replaceFile(filePath, new_content, now, now);
        } else {
            cache.addFile(filePath, new_content, now, now);
        }
    }

    public Cache getCache(){
        return cache;
    }

}
