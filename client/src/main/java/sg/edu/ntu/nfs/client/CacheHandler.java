package sg.edu.ntu.nfs.client;

public class CacheHandler {

    static Cache cache = new Cache(); // only one instance
    private final Proxy stub;

    public CacheHandler(Proxy stub, long fresh_t) {
        this.stub = stub;
        cache.setFreshT(fresh_t);
    }

    /**
     * Get file from cache
     * if file not in cache, request file and cache it
     * validate the file upon access
     *
     * @param file_path file path on server
     */

    public byte[] getFile(String file_path) {
        CacheEntry entry = cache.getFile(file_path);
        // not in cache
        if (entry == null){
            byte[] file_content = stub.requestFile(file_path);
            // file found on server
            if (file_content != null) {
                long t_mclient = System.currentTimeMillis();
                long t_c = System.currentTimeMillis();
                cache.addFile(file_path, file_content, t_mclient, t_c);
                return file_content;
            } else return null;
        }
        // in cache
        else {
            // check freshness upon access
            if(System.currentTimeMillis() - entry.getTc() >= cache.getFreshT()){
                long[] attr = stub.getattr((file_path));
                long t_mserver = attr[0];
                // invalid entry
                if(entry.getTmclient() < t_mserver){
                    // update entry
                    byte[] file_content = stub.requestFile(file_path);
                    // file still on server
                    if (file_content != null) {
                        long t_mclient = System.currentTimeMillis();
                        long t_c = System.currentTimeMillis();
                        cache.replaceFile(file_path, file_content, t_mclient, t_c);
                    }
                    // file no longer available on server
                    else cache.removeFile(file_path);
                }
            }
        }
        return entry.getFileContent();
    }

    public Cache getCache(){
        return cache;
    }



    /**
     * Update server after write
     * if successful, update cache
     *
     * @param file_path   file path on server
     * @param new_content new file content
     */

    /*
    public int write_file(String file_path, int offset, int count, byte[] new_content) {
        int response = stub.requestWrite(file_path, offset, count, new_content);
        // if write to sever was successful
        //long t_mclient = System.currentTimeMillis();
        //long t_c = System.currentTimeMillis();
        //cache.replaceFile(file_path, new_content, t_mclient, t_c);
        return response;
    }

     */
}
