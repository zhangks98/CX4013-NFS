package sg.edu.ntu.nfs.client;

public class FileOperations {
    private final OutRequestHandler out_req_handler;
    private final CacheHandler cache_handler;
    public FileOperations(OutRequestHandler out_req_handler){
        this.out_req_handler = out_req_handler;
        this.cache_handler = new CacheHandler(out_req_handler);
    }

    public void read(String file_path, int offset, int count){
        byte[] file = cache_handler.get_cached_file(file_path);
        // TODO: read
    }

    public void write(String file_path, int offset, String data){
        byte[] file = cache_handler.get_cached_file(file_path);
        // TODO: write
        byte[] new_file = file;
        cache_handler.update_cached_file(file_path, new_file);
    }
}
