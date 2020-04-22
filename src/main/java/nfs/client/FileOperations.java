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
        if (offset < 0) {
            logger.warn("Invalid argument for read: offset < 0");
            return Optional.empty();
        }
        if (count < 0) {
            logger.warn("Invalid argument for read: count < 0");
            return Optional.empty();
        }
        filePath = Paths.get(filePath).normalize().toString();
        Optional<byte[]> optFile = cacheHandler.getFile(filePath);
        return optFile.map(file -> {
            if (offset > file.length) {
                logger.warn("Offset out of range");
                return null;
            }
            byte[] slice;
            if (file.length - offset < count) {
                logger.warn("Count out of range, returning available bytes:");
                slice = Arrays.copyOfRange(file, offset, file.length);
            } else {
                slice = Arrays.copyOfRange(file, offset, offset + count);
            }
            System.out.println(new String(slice, StandardCharsets.UTF_8));
            return slice;
        });
    }

    /**
     * Starting from the given offset, read all bytes from a file,
     *
     * @param filePath file path on server
     * @param offset   offset in bytes
     * @return an Optional object of bytes
     */
    public Optional<byte[]> read(String filePath, int offset) throws IOException {
        if (offset < 0) {
            logger.warn("Invalid argument for read: offset < 0");
            return Optional.empty();
        }
        filePath = Paths.get(filePath).normalize().toString();
        Optional<byte[]> optFile = cacheHandler.getFile(filePath);
        return optFile.map(file -> {
            if (offset > file.length) {
                logger.warn("Offset out of range");
                return null;
            }
            byte[] slice = Arrays.copyOfRange(file, offset, file.length);
            System.out.println(new String(slice, StandardCharsets.UTF_8));
            return slice;
        });
    }

    /**
     * Write to a file by inserting data at the offset
     *
     * @param filePath file path on hte server
     * @param offset   offset in bytes
     * @param data     data in bytes
     * @throws IOException
     */
    public void insert(String filePath, int offset, byte[] data) throws IOException {
        if (offset < 0) {
            logger.warn("Invalid argument for insert: offset < 0");
            return;
        }
        filePath = Paths.get(filePath).normalize().toString();
        cacheHandler.insertFile(filePath, offset, data);
    }

    /**
     * Write to a file by appending data at the end
     *
     * @param filePath file path on hte server
     * @param data     data in bytes
     * @throws IOException
     */
    public void append(String filePath, byte[] data) throws IOException {
        filePath = Paths.get(filePath).normalize().toString();
        cacheHandler.appendFile(filePath, data);
    }


}