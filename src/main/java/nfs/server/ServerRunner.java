package nfs.server;

import nfs.common.Serializer;
import nfs.common.exceptions.BadRequestException;
import nfs.common.exceptions.NotFoundException;
import nfs.common.requests.Request;
import nfs.common.requests.RequestBuilder;
import nfs.common.responses.GenericResponse;
import nfs.common.responses.Response;
import nfs.common.responses.ResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

@Command(description = "The server for remote file access.", name = "server", mixinStandardHelpOptions = true)
public class ServerRunner implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger();
    @Parameters(index = "0", description = "The port of the file server.")
    private int port;
    @Parameters(index = "1", description = "The root directory for the file server.")
    private File rootDir;
    @Option(names = {"-m", "--mode"},
            required = true,
            description = {"The invocation semantic of the server. Valid values:",
                    "ALO\tAt-least-once invocation semantic",
                    "AMO\tAt-most-once invocation semantic "})
    private InvocationSemantics mode;
    private DatagramSocket socket;
    private Servicer servicer;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ServerRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        socket = new DatagramSocket(port);
        switch (mode) {
            case ALO:
                servicer = new AtLeastOnceServicer(rootDir);
                break;
            case AMO:
                servicer = new AtMostOnceServicer(rootDir);
                break;
            default:
                return 1;
        }
        logger.info(String.format("Server running at port %d", port));
        logger.info(String.format("Root directory: %s", rootDir.getAbsolutePath()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
        }));
        serve();
        return 0;
    }

    private void serve() throws IOException {
        while (true) {
            DatagramPacket request = new DatagramPacket(new byte[Serializer.BUF_SIZE], Serializer.BUF_SIZE);
            socket.receive(request);

            // Unmarshal the request.
            Request req = RequestBuilder.parseFrom(request.getData());
            Context ctx = new Context(request.getAddress(), request.getPort());
            logger.info(String.format("Received: %s", req.getName()));

            try {
                // Handle the request.
                Response res = servicer.handle(req, ctx);
                // Marshall the response and send to client.
                DatagramPacket response = new DatagramPacket(res.toBytes(), Serializer.BUF_SIZE,
                        ctx.getAddress(), ctx.getPort());
                socket.send(response);
            } catch (BadRequestException exp) {
                logger.warn(String.format("Bad request for %s", req.getName()), exp);
                DatagramPacket response = new DatagramPacket(
                        new GenericResponse(req.getId(), ResponseStatus.BAD_REQUEST).toBytes(), Serializer.BUF_SIZE,
                        ctx.getAddress(), ctx.getPort());
                socket.send(response);
            } catch (NotFoundException exp) {
                logger.warn(String.format("Not found for %s", req.getName()), exp);
                DatagramPacket response = new DatagramPacket(
                        new GenericResponse(req.getId(), ResponseStatus.NOT_FOUND).toBytes(), Serializer.BUF_SIZE,
                        ctx.getAddress(), ctx.getPort());
                socket.send(response);
            } catch (Exception exp) {
                logger.error(String.format("Error handling %s", req.getName()), exp);
                DatagramPacket response = new DatagramPacket(
                        new GenericResponse(req.getId(), ResponseStatus.INTERNAL_ERROR).toBytes(), Serializer.BUF_SIZE,
                        ctx.getAddress(), ctx.getPort());
                socket.send(response);
            }
        }
    }
}
