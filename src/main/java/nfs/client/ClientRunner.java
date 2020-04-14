package nfs.client;

import nfs.common.responses.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;

@Command(description = "The client for remote file access.", name = "nfs-client", mixinStandardHelpOptions = true)
public class ClientRunner implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger();
    String interfaceMsg = "\n================== Client User Interface =================\n"
            + "The following commands are available:     \n"
            + "<> - required arguments\n"
            + "[] - optional arguments\n\n"
            + "| read <file path> <offset> <count>                  |\n"
            + "| write <file path> <offset> <data>                  |\n"
            + "| register <file path> <monitor interval (ms)>       |\n"
            + "| touch <new file path>                              |\n"
            + "| ls [dir]                                           |\n"
            + "| help                                               |\n"
            + "| exit                                               |";
    private final Scanner sc = new Scanner(System.in);
    private FileOperations fileOp;
    private Proxy stub;
    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;
    @Parameters(index = "1", description = "The port of the file server.")
    private int port;
    @Option(names = {"-f", "--fresh-interval"}, defaultValue = "10000",
            description = "Freshness interval (in ms) of the client cache. Default value: ${DEFAULT-VALUE}")
    private long freshInterval;
    @Option(names = {"-l", "--loss-prob"}, defaultValue = "0",
            description = "Probability of a request loss. Default value: ${DEFAULT-VALUE}")
    private double lossProb;


    public static void main(String... args) {
        int exitCode = new CommandLine(new ClientRunner()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Check if an input string is a string of integer
     *
     * @param strInput input string
     * @return true if a number can be parsed from the string
     */
    public boolean containsNum(String strInput) {
        try {
            Integer.parseInt(strInput);
        } catch (NumberFormatException e) {
            logger.warn(strInput + " is not a numerical value");
            return false;
        } catch (Exception e) {
            logger.warn("Exception: " + e);
            return false;
        }
        return true;
    }

    /**
     * Check if a command is of the correct length
     *
     * @param command input command
     * @param len     desired length
     * @return true if the command is of the desired length
     */
    public boolean validateLength(String[] command, int len) {
        if (command.length != len) {
            logger.warn("The command has missing/extra arguments");
            return false;
        }
        return true;
    }

    /**
     * Identify the command type and call the stub to invoke corresponding methods
     *
     * @param command input command
     */
    public void processCommand(String[] command) {
        if (command.length == 0 || command[0].equals(""))
            return;

        try {
            switch (command[0]) {
                case "read":
                    if (validateLength(command, 4) && containsNum(command[2]) && containsNum(command[3]))
                        fileOp.read(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));
                    break;
                case "write":
                    if (validateLength(command, 4) && containsNum(command[2]))
                        stub.write(command[1], Integer.parseInt(command[2]), command[3].getBytes());
                    break;
                case "touch":
                    if (validateLength(command, 2))
                        stub.touch(command[1]);
                    break;
                case "ls":
                    if (command.length == 1)
                        stub.listDir(".");
                    else if (validateLength(command, 2))
                        stub.listDir(command[1]);
                    break;
                case "register":
                    if (validateLength(command, 3) && containsNum(command[2])) {
                        int monitorInterval = Integer.parseInt(command[2]);
                        stub.register(command[1], monitorInterval);
                    }
                    break;
                case "help":
                    System.out.println(interfaceMsg);
                    break;
                default:
                    logger.warn("Invalid commands, please try again");
                    break;
            }

        } catch (IOException e) {
            logger.warn("Error " + command[0] + ": " + e);
        }
    }

    @Override
    public Integer call() throws Exception {
        // Queue for synchronizing register request.
        SynchronousQueue<Response> queue = new SynchronousQueue<>(/*fair=*/true);
        // Socket for handling callbacks.
        DatagramSocket callbackSocket = new DatagramSocket();

        stub = new Proxy(address, port, callbackSocket, queue);
        CacheHandler cacheHandler = new CacheHandler(stub, freshInterval);
        CallbackHandler callbackHandler = new CallbackHandler(cacheHandler, callbackSocket, queue);
        Thread callbackThread = new Thread(callbackHandler);
        fileOp = new FileOperations(cacheHandler);

        callbackThread.start();
        logger.info("Callback handler thread started");

        System.out.println(interfaceMsg);
        System.out.println();

        while (true) {
            System.out.print("nfs-client> ");
            String userInput = sc.nextLine();
            if (userInput.trim().equals("exit")) {
                stub.close();
                callbackThread.interrupt();
                break;
            }
            String[] split_input = userInput.trim().split(" ");
            processCommand(split_input);
            System.out.println();
        }

        return 0;
    }
}
