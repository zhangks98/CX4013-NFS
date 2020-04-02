
package sg.edu.ntu.nfs.client;

import java.util.Arrays;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileOperations {
    private static final Logger logger = LogManager.getLogger();
    private final CacheHandler cache_handler;

    public FileOperations(CacheHandler cache_handler) {
        this.cache_handler = cache_handler;
    }

    /**
     * Starting from the given offset, read the given number of bytes from a file,
     * @param file_path file path on server
     * @param offset offset in bytes
     * @param count byte counts of content requested
     * @return an Optional object of bytes
     */
    public Optional<byte[]> read(String file_path, int offset, int count) {

        byte[] file = cache_handler.getFile(file_path);
        Optional<byte[]> opt = Optional.empty();

        if (file != null) {
            if (offset >= file.length) {
                logger.warn("Offset out of range");

            } else if (file.length - offset < count) {
                byte[] slice = Arrays.copyOfRange(file, offset, file.length);
                logger.warn("Count out of range, returning available bytes:\n" + slice.toString());
                opt = Optional.of(slice);

            } else {
                byte[] slice = Arrays.copyOfRange(file, offset, offset + count);
                logger.info(slice.toString());
                opt = Optional.of(slice);
            }

        } else {
            logger.error(file_path + " is no longer available on server");
        }
        return opt;
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