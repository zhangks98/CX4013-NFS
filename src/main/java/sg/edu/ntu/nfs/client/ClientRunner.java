package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(description = "The client for remote file access.", name = "nfs-client", mixinStandardHelpOptions = true)
public class ClientRunner implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger();
    Scanner sc = new Scanner(System.in);
    FileOperations fileOp;
    Proxy stub;
    CacheHandler cacheHandler;
    CallbackReceiver callbackReceiver;

    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;
    @Parameters(index = "1", description = "The port of the file server.")
    private int port;
    @Parameters(index = "2", description = "Freshness interval of the client cache")
    private long freshness_interval;

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

    public static void main(String... args) {
        int exitCode = new CommandLine(new ClientRunner()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Check if an input string is a string of integer
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
     * @param command input command
     * @param len desired length
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
     * @param command input command
     */
    public void processCommand(String[] command) {
        if (command.length == 0 || command[0].equals(""))
            return;

        try{
            if (command[0].equals("read")) {
                if(validateLength(command, 4) && containsNum(command[2]) && containsNum(command[3]))
                    fileOp.read(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));

            } else if (command[0].equals("write")) {
                if (validateLength(command, 4) && containsNum(command[2]))
                    stub.write(command[1], Integer.parseInt(command[2]), command[3].getBytes());

            } else if (command[0].equals("touch")) {
                if (validateLength(command, 2))
                    stub.touch(command[1]);

            } else if (command[0].equals("ls")) {
                if (command.length == 1)
                    stub.listDir(".");
                else if (validateLength(command, 2))
                    stub.listDir(command[1]);

            } else if (command[0].equals("register")) {
                if (validateLength(command, 3) && containsNum(command[2])) {
                    int monitorInterval = Integer.parseInt(command[2]);
                    stub.register(command[1], monitorInterval);
                    callbackReceiver.run(monitorInterval);
                }

            } else if (command[0].equals("help")) {
                System.out.println(interfaceMsg);
                System.out.println();

            } else {
                logger.warn("Invalid commands, please try again");
            }

        } catch (IOException e) {
            logger.warn("Error " + command[0] + ": " + e);
        }
    }

    @Override
    public Integer call() throws Exception {
        stub = new Proxy(address, port);
        cacheHandler = new CacheHandler(stub, 1000);
        fileOp = new FileOperations(cacheHandler);
        callbackReceiver = new CallbackReceiver(cacheHandler);

        System.out.println(interfaceMsg);
        System.out.println();

        while (true) {
            String userInput = sc.nextLine();
            if (userInput.trim().equals("exit"))
                break;
            String[] split_input = userInput.trim().split(" ");
            processCommand(split_input);
        }

        return 0;
    }
}
