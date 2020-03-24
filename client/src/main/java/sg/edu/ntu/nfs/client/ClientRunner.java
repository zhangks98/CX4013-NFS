package sg.edu.ntu.nfs.client;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(description = "The client for remote file access.", name = "client", mixinStandardHelpOptions = true)
public class ClientRunner implements Callable<Integer> {

    Scanner sc = new Scanner(System.in);
    FileOperations file_op;
    Proxy stub;
    CacheHandler cache_handler;
    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;
    @Parameters(index = "1", description = "The port of the file server.")
    private int port;

    public static void main(String... args) {
        int exitCode = new CommandLine(new ClientRunner()).execute(args);
        System.exit(exitCode);
    }

    public boolean contains_num(String str_input) {
        try {
            Integer.parseInt(str_input);
        } catch (NumberFormatException e) {
            System.out.println(str_input + " is not a numerical value");
            return false;
        } catch (Exception e) {
            System.out.print("Exception: " + e);
            return false;
        }
        return true;
    }

    public boolean validate_length(String[] command, int len) {
        if (command.length != len) {
            System.out.println("The command has missing/extra arguments");
            return false;
        }
        return true;
    }


    public void processCommand(String[] command) {
        boolean valid;
        if (command[0].equals("read")) {
            if(validate_length(command, 4) && contains_num(command[2]) && contains_num(command[3]))
                file_op.read(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));

        } else if (command[0].equals("write")) {
            if (validate_length(command, 5) && contains_num(command[2]) && contains_num(command[3]))
                stub.write(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]), command[4].getBytes());

        } else if (command[0].equals("touch")) {
            if (validate_length(command, 2))
                stub.touch(command[1]);

        } else if (command[0].equals("ls")) {
            if (validate_length(command, 2))
                stub.listDir(command[1]);

        } else if (command[0].equals("register")) {
            if (validate_length(command, 3) && contains_num(command[2]))
                stub.register(command[1], Integer.parseInt(command[2]));
          
        } else {
            System.out.println("Invalid commands, please try again");
        }
    }

    @Override
    public Integer call() throws Exception {
        stub = new Proxy(address, port);
        // init cache with freshness interval.
        cache_handler = new CacheHandler(stub, 1000);
        file_op = new FileOperations(cache_handler);

        String interface_msg = "\n=========== Client User Interface ==========\n"
                + "The following commands are available:\n"
                + "| read [file_path] [offset] [count]       |\n"
                + "| write [file_path] [offset] [data]       |\n"
                + "| register [file_path]                    |\n"
                + "| touch [new_file_path]                   |\n"
                + "| ls [dir]                                |";
        System.out.println(interface_msg);
        String user_input = sc.nextLine();
        String[] split_input = user_input.trim().split(" ");
        processCommand(split_input);
        return 0;
    }
}
