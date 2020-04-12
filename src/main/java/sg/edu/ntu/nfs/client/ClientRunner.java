package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class ClientRunner implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger();
    Scanner sc = new Scanner(System.in);
    FileOperations file_op;
    Proxy stub;
    CacheHandler cache_handler;

    private InetAddress address;
    private int port;
    private long freshness_interval;

    public ClientRunner(InetAddress address, int port, long freshness_interval) {
        this.address = address;
        this.port = port;
        this.freshness_interval = freshness_interval;
    }

    String interface_msg = "\n============= Client User Interface ============\n"
            + "The following commands are available:     \n"
            + "<> - required arguments\n"
            + "[] - optional arguments\n\n"
            + "| read <file_path> <offset> <count>       |\n"
            + "| write <file_path> <offset> <data>       |\n"
            + "| register <file_path> <monitor_interval> |\n"
            + "| touch <new_file_path>                   |\n"
            + "| ls [dir]                                |\n"
            + "| help                                    |\n"
            + "| exit                                    |";

    /**
     * Check if an input string is a string of integer
     * @param str_input input string
     * @return true if a number can be parsed from the string
     */
    public boolean containsNum(String str_input) {
        try {
            Integer.parseInt(str_input);
        } catch (NumberFormatException e) {
            logger.warn(str_input + " is not a numerical value");
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
                    file_op.read(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));

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
                if (validateLength(command, 3) && containsNum(command[2]))
                    stub.register(command[1], Integer.parseInt(command[2]));

            } else if (command[0].equals("help")) {
                System.out.println(interface_msg);
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
        // init cache with freshness interval.
        cache_handler = new CacheHandler(stub, freshness_interval);
        file_op = new FileOperations(cache_handler);

        System.out.println(interface_msg);
        System.out.println();

        while (true) {
            String user_input = sc.nextLine();
            if (user_input.trim().equals("exit"))
                break;
            String[] split_input = user_input.trim().split(" ");
            processCommand(split_input);
        }
        return 0;
    }
}
