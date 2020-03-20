package sg.edu.ntu.nfs.client;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sg.edu.ntu.nfs.common.requests.ListDirRequest;
import sg.edu.ntu.nfs.common.responses.Response;
import sg.edu.ntu.nfs.common.responses.ResponseStatus;
import sg.edu.ntu.nfs.common.values.Value;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(description = "The client for remote file access.", name = "client", mixinStandardHelpOptions = true)
public class ClientRunner implements Callable<Integer> {

    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;

    @Parameters(index = "1", description = "The port of the file server.")
    private int port;

    Scanner sc = new Scanner(System.in);
    private String interface_msg =
            "\n=========== Client User Interface ==========\n"
                    + "The following commands are available:\n"
                    + "| read [file_path] [offset] [count]       |\n"
                    + "| write [file_path] [offset] [data]       |\n"
                    + "| register [file_path]                    |\n"
                    + "| touch [new_file_path]                   |\n"
                    + "| ls [dir]                                |";

    FileOperations file_op = new FileOperations();
    OutRequestHandler out_req_handler = new OutRequestHandler();
    // init cache with freshness interval
    CacheHandler cache_handler = new CacheHandler(1000);

    public boolean contains_num(String str_input){
        try{
            Integer.parseInt(str_input);
        }
        catch(NumberFormatException e){
            System.out.println(str_input + " is not a numerical value");
            return false;
        }
        catch (Exception e){
            System.out.print("Exception: " + e);
            return false;
        }
        return true;
    }

    public boolean validate_read_command(String[] command, int len){
        // check number of arguments
        if(command.length != len){
            System.out.println("The read command has missing/extra arguments");
            return false;
        }
        return contains_num(command[2]) && contains_num(command[3]);
    }

    public boolean validate_write_command(String[] command, int len){
        // check number of arguments
        if(command.length != len){
            System.out.println("The write command has missing/extra arguments");
            return false;
        }
        return contains_num(command[2]);
    }

    public boolean validate_length(String[] command, int len){
        if(command.length != len){
            System.out.println("The command has missing/extra arguments");
            return false;
        }
        return true;
    }

    public void processCommand(String[] command) throws Exception{
        boolean valid;
        if (command[0].equals("read")){
            valid = validate_read_command(command, 4);
            if(valid)
                file_op.read(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));
        }
        else if (command[0].equals("write")){
            valid = validate_write_command(command, 4);
            if (valid)
                file_op.write(command[1], Integer.parseInt(command[2]), command[3]);
        }
        else if (command[0].equals("touch")){
            valid = validate_length(command, 2);
            if(valid)
                out_req_handler.requestTouch(command[1]);
        }
        else if (command[0].equals("ls")){
            valid = validate_length(command, 2);
            if(valid)
                out_req_handler.listDir(command[1]);
        }
        else if (command[0].equals("register")){
            valid = validate_length(command, 2);
            if(valid)
                out_req_handler.register(command[1]);
        }
        else{
            System.out.println("Invalid commands, please try again");
        }
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new ClientRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        //Proxy stub = new Proxy(address, port);
        out_req_handler.set_proxy(address, port);

        System.out.println(this.interface_msg);
        String user_input = sc.nextLine();
        String[] split_input = user_input.trim().split(" ");
        processCommand(split_input);
        /*
        Response res = stub.invoke(new ListDirRequest(""));
        if (res.getStatus() == ResponseStatus.OK) {
            for (Value val : res.getValues()) {
                String filename = (String) val.getVal();
                System.out.println(filename);
            }
        }
         */
        return 0;
    }
}
