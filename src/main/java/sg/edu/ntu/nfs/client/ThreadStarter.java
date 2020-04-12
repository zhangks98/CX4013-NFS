package sg.edu.ntu.nfs.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Command(description = "The thread starter on client for remote file access.",
        name = "thread_starter", mixinStandardHelpOptions = true)

public class ThreadStarter implements Callable<Integer> {

    // For Run Configuration:
    // program param example: localhost 8888 10000

    @Parameters(index = "0", description = "The address of the file server.")
    private InetAddress address;
    @Parameters(index = "1", description = "The port of the file server.")
    private int server_port;
    @Parameters(index = "2", description = "Freshness interval for files in the client cache")
    private long freshness_interval;

    private static final Logger logger = LogManager.getLogger();

    /**
     * Run the client main thread and callback thread util the main thread is terminated
     */
    private void startThreads() {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(2);
            ClientRunner clientRunner = new ClientRunner(address, server_port, freshness_interval);
            CallbackReceiver callbackReceiver = new CallbackReceiver();

            Future<Integer> client_future = executorService.submit(clientRunner);
            Future<Integer> callback_future = executorService.submit(callbackReceiver);
            // stop submission of more threads
            executorService.shutdown();
            logger.info("Started client main thread and callback thread");

            while (!client_future.isDone()) {
                Thread.sleep(1000);
            }
            executorService.shutdownNow();
            logger.info("Client main thread and callback thread terminated.");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ThreadStarter()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        startThreads();
        return 0;
    }
}
