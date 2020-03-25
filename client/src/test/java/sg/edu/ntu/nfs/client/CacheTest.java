package sg.edu.ntu.nfs.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class CacheTest {
    Cache cache = new Cache();

    @Test
    public void addEntry(){
        String file_path = "textA.txt";
        byte[] content = "This is text A".getBytes();
        long t_mclient = 1000;
        long t_c = 1200;
        cache.addFile(file_path, content, t_mclient, t_c);
        CacheEntry cached = cache.getFile(file_path);
        assertEquals(cached.getFileContent(), content);
        assertEquals(cached.getTmclient(), t_mclient);
        assertEquals(cached.getTc(), t_c);
    }

    @Test
    public void getNonCachedFile(){
        String file_path = "textB.txt";
        CacheEntry cached = cache.getFile(file_path);
        assertEquals(cached, null);
    }

    @Test
    public void replaceEntry(){
        String file_path = "textA.txt";
        byte[] content = "This is text A".getBytes();
        byte[] new_content = "This is text C".getBytes();
        long t_mclient = 1000;
        long t_c = 1200;
        cache.addFile(file_path, content, t_mclient, t_c);
        cache.replaceFile(file_path, new_content, t_mclient, t_c);
        CacheEntry cached = cache.getFile(file_path);
        assertEquals(cached.getFileContent(), new_content);
        assertEquals(cached.getTmclient(), t_mclient);
        assertEquals(cached.getTc(), t_c);
    }

    @Test
    public void removeEntry(){
        String file_path = "textA.txt";
        byte[] content = "This is text A".getBytes();
        long t_mclient = 1000;
        long t_c = 1200;
        cache.addFile(file_path, content, t_mclient, t_c);
        cache.removeFile(file_path);
        CacheEntry cached = cache.getFile(file_path);
        assertEquals(cached, null);
    }


}
