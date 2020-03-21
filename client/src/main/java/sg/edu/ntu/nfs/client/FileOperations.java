
package sg.edu.ntu.nfs.client;

import java.util.Arrays;

public class FileOperations {
    private final CacheHandler cache_handler;

    public FileOperations(CacheHandler cache_handler) {
        this.cache_handler = cache_handler;
    }

    public void read(String file_path, int offset, int count) {
        byte[] file = cache_handler.get_file(file_path);
        if (file != null){
            if (offset > file.length){
                System.out.println("Offset out of range");
            }else if (file.length - offset < count){
                byte[] slice = Arrays.copyOfRange(file, offset, file.length-1);
                System.out.println("Count out of range, returning available bytes:");
                System.out.println(slice.toString());
            }else{
                byte[] slice = Arrays.copyOfRange(file, offset, offset + count -1);
                System.out.println(slice);
            }
        }else{
            System.out.println(file_path + " is no longer available on server");
        }
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