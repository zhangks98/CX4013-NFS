
package sg.edu.ntu.nfs.client;

import java.io.IOException;
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
     * @param filePath file path on server
     * @param offset offset in bytes
     * @param count byte counts of content requested
     * @return an Optional object of bytes
     */
    public Optional<byte[]> read(String filePath, int offset, int count) throws IOException {

        Optional<byte[]> opt_file = cache_handler.getFile(filePath);
        Optional<byte[]> opt_slice = Optional.empty();

        if (opt_file.isPresent()) {
            byte[] file = opt_file.get();

            if (file != null) {
                if (offset >= file.length) {
                    logger.warn("Offset out of range");

                } else if (file.length - offset < count) {
                    byte[] slice = Arrays.copyOfRange(file, offset, file.length);
                    logger.warn("Count out of range, returning available bytes:\n" + slice.toString());
                    opt_slice = Optional.of(slice);

                } else {
                    byte[] slice = Arrays.copyOfRange(file, offset, offset + count);
                    logger.info(slice.toString());
                    opt_slice = Optional.of(slice);
                }

            } else {
                logger.error(filePath + " is not found on server");
            }
        }
        return opt_slice;
    }
    
}