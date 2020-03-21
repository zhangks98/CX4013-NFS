
package sg.edu.ntu.nfs.client;

public class FileOperations {
    private final CacheHandler cache_handler;

    public FileOperations(CacheHandler cache_handler) {
        this.cache_handler = cache_handler;
    }

    public void read(String file_path, int offset, int count) {
        byte[] file = cache_handler.get_file(file_path);
        // TODO: read
    }


    /*
    public void write(String file_path, int offset, String data) {
        // send request
        // then write file
        byte[] file = cache_handler.get_file(file_path);
        byte[] new_file = file;
        cache_handler.update_cached_file(file_path, new_file);
    }
     */
}