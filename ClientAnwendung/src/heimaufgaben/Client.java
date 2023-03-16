package heimaufgaben;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This is the Client-class that ensures its functionality and the connection with the local Server.
 */
public class Client {
    private Socket clientSocket;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String address;

    /**
     * This method connects the client to the local server.
     */
    public void connect() {
        // creates a Client-socket
        clientSocket = CreateSocket();

        if (clientSocket != null) {
            System.out.println();
            System.out.println("A TCP-connection to the server with  IP-Address " + address + " (Port: 2022) is\r\n"
                    + "made. You can now give the server your commands.\r\n" + "");
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            } catch (IOException e) {
                System.out.println("The connection to the server was interrupted !!");
            }
        }
    }

    /**
     * This method makes sure to disconnect the Client from the Server and resets the clientSocket-value to null.
     */
    public void disconnect() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            clientSocket = null;
            System.out.println("The connection to the server is closed.");
        } catch (IOException e) {
            System.out.println();
        }
    }

    /**
     * This method creates the client socket with port = 2022 and IP-Address = 127.0.0.1
     *
     * @return the created socket
     */
    public Socket CreateSocket() {
        // variables that represent the socket that will be created
        // with the port(= 2022)
        Socket socket = null;

        // takes the input of the IP-Address
        Scanner input = new Scanner(System.in);
        System.out.print("IP-Address: ");
        address = input.nextLine();

        // checks the IP-Address
        if (address.equals("localhost") || address.equals("127.0.0.1")) {

            // takes the input of the Port
            System.out.print("Port: ");
            String port = input.nextLine();

            if (port.equals("2022")) {
                try {
                    // creates a socket with the given IP-Address and port-number
                    socket = new Socket(address, Integer.parseInt(port));
                } catch (IOException e) {
                    System.out.println("Error !! The TCP-connection with the Server using\r\n"
                            + "IP-Address " + address + " (Port: 2022) was not successfully made.");
                }
            } else {
                System.out.println("The port-number is not correct !!");
                System.out.println("Only port number 2022 is possible.");
            }

        } else {
            System.out.println("The IP-Address is not correct !!");
            System.out.println("Only IPv4-Address 127.0.0.1 and localhost are possible inputs.");
        }

        return socket;
    }

    /**
     * Checks the status of the connection.
     *
     * @return True, if the connection is established. Otherwise, False.
     */
    public boolean isConnected() {
        return (clientSocket != null && clientSocket.isConnected());
    }

    /**
     * This method gives back the reply from the Server.
     *
     * @param clientInput the command from the Client
     * @return the reply from the Server
     */
    public String requestFromServer(String clientInput) {
        out.println(clientInput);
        String reply = "";
        try {
            reply = in.readLine();
        } catch (IOException e) {
            System.out.println("The connection to the server was interrupted !!");
            clientSocket = null;
        }
        return reply;
    }

    /**
     * This method extracts the message that will be shown from the returned Server-message.
     *
     * @param reply   the returned Server-message
     * @param command the given command from the client
     * @return the shown message to the Client
     */
    public String extractMessage(String reply, String command) {
        StringBuilder msg = new StringBuilder();
        String[] reply_parts = reply.split(" ");
        String[] command_parts = command.split(" ");

        // removes only the first word from the output of th server
        if ((reply_parts[0].equals("ECHO") || reply_parts[0].equals("TIME") ||
                reply_parts[0].equals("DATE") || reply_parts[0].equals("ERROR")) && !command.equals("HISTORY")) {
            msg.append(String.join(" ", Arrays.copyOfRange(reply_parts, 1, reply_parts.length)));
        } else if (command_parts[0].equals("HISTORY")) {
            String[] history_list_parts = reply.split("/");
            for (String historyListPart : history_list_parts) msg.append(historyListPart).append("\n");
        } else {
            msg.append(reply);
        }

        return msg.toString();
    }
}
