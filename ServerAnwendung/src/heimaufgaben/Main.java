package heimaufgaben;

import java.util.*;

/**
 * This is the main Server-class.
 */
public class Main {
    private static final Server server = new Server();
    private static final Scanner serverInput = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("Port: ");
        String port = serverInput.nextLine();
        System.out.println();

        if (!server.checkPort(port)) {
            System.out.println("The port number is not correct !!");
            System.out.println("Only port number 2022 is possible.");
        } else {
            System.out.println("The server has started and waits in the Port " + port + " commands from the Client.");

            // starts the answerThread and waits the input to end the server
            startThread();

            // starts the execution of the server
            server.startServer();
        }

        System.out.println();
        System.out.println("The server is closed.");
    }

    /**
     * This method starts a thread that outputs the command line query to stop the server.
     */
    private static void startThread() {
        Thread answerThread = new Thread(() -> {
            char answer;
            do {
                System.out.print("If you want to end the server, write \"J\" : ");
                answer = serverInput.next().charAt(0);
            }
            while (answer != 'J');
            server.disconnect();
        });
        answerThread.start();
    }
}
