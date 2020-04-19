package nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class FileOperations {
    private static final Logger logger = LogManager.getLogger();
    private final CacheHandler cacheHandler;

    public FileOperations(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    /**
     * Starting from the given offset, read the given number of bytes from a file,
     *
     * @param filePath file path on server
     * @param offset   offset in bytes
     * @param count    byte counts of content requested
     * @return an Optional object of bytes
     */
    public Optional<byte[]> read(String filePath, int offset, int count) throws IOException {
        filePath = Paths.get(filePath).normalize().toString();
        Optional<byte[]> optFile = cacheHandler.getFile(filePath);
        byte[] slice = null;

        if (optFile.isPresent()) {
            byte[] file = optFile.get();
            if (offset >= file.length) {
                logger.warn("Offset out of range");
            } else if (file.length - offset < count) {
                slice = Arrays.copyOfRange(file, offset, file.length);
                logger.warn("Count out of range, returning available bytes:");
                System.out.println(new String(slice, StandardCharsets.UTF_8));
            } else {
                slice = Arrays.copyOfRange(file, offset, offset + count);
                System.out.println(new String(slice, StandardCharsets.UTF_8));
            }
        }
        return Optional.ofNullable(slice);
    }

    /**
     * Write to a file by inserting data at the offset
     *
     * @param filePath file path on hte server
     * @param offset   offset in bytes
     * @param data     data in bytes
     * @throws IOException
     */
    public void write(String filePath, int offset, byte[] data) throws IOException {
        filePath = Paths.get(filePath).normalize().toString();
        cacheHandler.writeFile(filePath, offset, data);
    }

}