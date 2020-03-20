package sg.edu.ntu.nfs.client;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.responses.Response;
import sg.edu.ntu.nfs.common.responses.ResponseStatus;
import sg.edu.ntu.nfs.common.values.Value;

import java.net.InetAddress;
import java.util.concurrent.Callable;

@Command(description = "The client for remote file access.", name = "client", mixinStandardHelpOptions = true)
public class ClientRunner implements Callable<Integer> {

    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;

    @Parameters(index = "1", description = "The port of the file server.")
    private int port;

    public static void main(String... args) {
        int exitCode = new CommandLine(new ClientRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Put your main function here.
        Proxy stub = new Proxy(address, port);
        OutRequestHandler handler = new OutRequestHandler(stub);
        return 0;
    }
}
