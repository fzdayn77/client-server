package heimaufgaben;

import java.util.Scanner;

/**
 * This is the main Client-class.
 */
public class Main {
    private static final Client client = new Client();
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        client.connect();

        while (client.isConnected()) {
            System.out.print("$ ");
            String userInput = input.nextLine();

            if (userInput.equals("EXIT")) {
                client.disconnect();
            } else {
                // This is the reply from the Server
                String replyFromServer = client.requestFromServer(userInput);

                // This is the message/error that will be shown
                String message = client.extractMessage(replyFromServer, userInput);
                System.out.println(message);
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("The client is closed.");
    }
}
