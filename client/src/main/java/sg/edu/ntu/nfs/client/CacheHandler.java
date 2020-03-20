package sg.edu.ntu.nfs.client;

public class CacheHandler {
    static Cache cache = new Cache(); // only one instance used throughout
    OutRequestHandler out_req_handler = new OutRequestHandler(stub);

    public CacheHandler(){}
    public CacheHandler(long fresh_t){
        cache.setFreshT(fresh_t);
    }

    /**
     * Get file from cache
     * if file not in cache, request file and cache it
     * validate the file upon access
     * @param file_path file path on server
     */
    public byte[] get_cached_file(String file_path){
        CacheEntry entry = cache.getFile(file_path);
        // not in cache
        if (entry == null){
            byte[] file_content = out_req_handler.requestFile(file_path);
            // file found on server
            if(file_content != null){
                long t_mclient = System.currentTimeMillis();
                long t_c = System.currentTimeMillis();
                cache.addFile(file_path, file_content, t_mclient, t_c);
                return file_content;
            }
            else return null;
        }
        // in cache
        else{
            // check freshness upon access
            if(System.currentTimeMillis() - entry.getTc() < cache.getFreshT()){
                // get t_mserver
                long[] attr = out_req_handler.getattr((file_path));
                // invalid entry
                if(entry.getTmclient() < attr[0]){  // TODO: allow a small difference
                    // update entry
                    byte[] file_content = out_req_handler.requestFile(file_path);
                    // file still on server
                    if(file_content != null){
                        long t_mclient = System.currentTimeMillis();
                        long t_c = System.currentTimeMillis();
                        cache.replaceFile(file_path, file_content, t_mclient, t_c);
                    }
                    // file no longer available on server
                    else cache.removeFile(file_path);
                }
                // valid entry, update validation time t_c
                else{
                    long t_c = System.currentTimeMillis();
                    cache.replaceFile(file_path, entry.getFileContent(), entry.getTmclient(), t_c);
                }
            }
        }
        return entry.getFileContent();
    }

    /**
     * Update server after write
     * if successful, update cache
     * @param file_path file path on server
     * @param new_content new file content
     */
    public int update_cached_file(String file_path, byte[] new_content){
        int response = out_req_handler.updateFile(file_path, new_content);
        // if write to sever was successful
        long t_mclient = System.currentTimeMillis();
        long t_c = System.currentTimeMillis();
        cache.replaceFile(file_path, new_content, t_mclient, t_c);
        return response;
    }
}
