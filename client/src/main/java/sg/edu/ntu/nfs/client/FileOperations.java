package sg.edu.ntu.nfs.client;

public class FileOperations {
    public FileOperations(){ }
    CacheHandler cache_handler = new CacheHandler();

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
