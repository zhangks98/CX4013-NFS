package sg.edu.ntu.nfs.server;

import org.junit.Before;
import org.junit.Test;
import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.requests.Request;
import sg.edu.ntu.nfs.common.responses.Response;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class AtMostOnceServicerTest {
    private Servicer servicer;

    @Before
    public void setUp() {
        servicer = new AtMostOnceServicer(new File((".")));
    }

    @Test
    public void duplicateRequestShouldGetTheSameReply() {
        Request req = new ListDirRequest(".");
        try {
            Context ctx = new Context(InetAddress.getByName("127.0.0.1"), 2222);
            Response resA = servicer.handle(req, ctx);
            Response resB = servicer.handle(req, ctx);
            assertEquals(resA.getReqId(), resB.getReqId());
        } catch (UnknownHostException e) {
            fail("Unknown host");
        } catch (Exception e) {
            fail();
        }
    }

}