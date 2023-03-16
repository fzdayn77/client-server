package heimaufgaben;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.*;

/**
 * This is the Server-class which contains all the functions that ensure the functionality of the local server.
 */
public class Server {
    private int port;
    private ServerSocket serverSocket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;

    // a list of all the commands within a single session
    private final ArrayList<String> history_list = new ArrayList<>();

    @SuppressWarnings("InfiniteLoopStatement")
    public void startServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            // contains the given command
            String command;

            // contains the answer from the Server depending on the given command
            String answer;

            // accepts the server socket
            Socket socket = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            while (true) {
                command = in.readLine();
                if (command == null) {
                    history_list.clear();
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException ignored) {
                    }
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                } else {
                    // adds the given command to the history list
                    history_list.add(command);

                    if (command.equals("PING")) {
                        answer = "PONG";
                    } else if (command.equals("CURRENT DATE")) {
                        answer = "DATE " + getDate();
                    } else if (command.equals("CURRENT TIME")) {
                        answer = "TIME " + getTime();
                    } else if (checkECHO(command)) {
                        answer = getECHO(command);
                    } else if (checkHISTORY(command)) {
                        answer = getHISTORY(history_list, command);
                    } else if (checkAPI_Request(command)) {
                        answer = getAPI(command);
                    } else if (checkOperation(command)) {
                        answer = getOperation(command);
                    } else {
                        answer = "ERROR 400 BAD REQUEST";
                    }
                    out.println(answer);
                }
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * This method ends the Server.
     */
    public void disconnect() {
        try {
            serverSocket.close();
            if (in != null) {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * THis method checks the port number.
     *
     * @param port the Port-input
     * @return true, if the Port=2022. Otherwise, false.
     */
    public boolean checkPort(String port) {
        boolean c = false;
        if (port.equals("2022")) {
            this.port = 2022;
            c = true;
        }
        return c;
    }

    /**
     * This method returns the current time (format: HH:mm:ss)
     *
     * @return the current time.
     */
    private static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method returns the current date (format: dd.MM.yyyy)
     *
     * @return the current date.
     */
    private static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method checks the correctness of the ECHO-command.
     *
     * @param str the given command.
     * @return true if the first word of given command is ECHO. Otherwise, false.
     */
    private boolean checkECHO(String str) {
        String[] str_parts = str.split(" ");
        return str_parts[0].equals("ECHO");
    }

    /**
     * @param str the given command.
     * @return the command.
     */
    private String getECHO(String str) {
        String[] str_parts = str.split(" ");
        return String.join(" ", Arrays.copyOfRange(str_parts, 0, str_parts.length));
    }

    /**
     * This method checks the correctness of the HISTORY-command.
     *
     * @param str the given command.
     * @return true, if the first word of str is HISTORY. Otherwise, false.
     */
    private boolean checkHISTORY(String str) {
        String[] str_parts = str.split(" ");
        return str_parts[0].equals("HISTORY");
    }

    /**
     * This method returns the history of all given commands.
     *
     * @param list the history list
     * @param str  the given command
     * @return the commands in the history list as a [String]
     */
    private String getHISTORY(ArrayList<String> list, String str) {
        String[] str_parts = str.split(" ");
        String reply;

        if (str_parts.length == 2) {
            if (isInteger(Integer.parseInt(str_parts[1]))) {
                if (Integer.parseInt(str_parts[1]) > list.size()) {
                    StringBuilder replyBuilder = new StringBuilder();
                    for (int k = 0; k < list.size() - 1; k++) {
                        replyBuilder.append(list.get(k)).append("/");
                    }
                    reply = replyBuilder.toString();
                } else {
                    StringBuilder replyBuilder = new StringBuilder();
                    for (int j = list.size() - Integer.parseInt(str_parts[1]); j < list.size() - 1; j++) {
                        replyBuilder.append(list.get(j)).append("/");
                    }
                    reply = replyBuilder.toString();
                }
            } else {
                // if the number is not an integer
                reply = "ERROR 400 BAD REQUEST";
            }
        } else if (str_parts.length == 1) {
            StringBuilder replyBuilder = new StringBuilder();
            for (int i = 0; i < list.size() - 1; i++) {
                replyBuilder.append(list.get(i)).append("/");
            }
            reply = replyBuilder.toString();
        } else {
            reply = "ERROR 400 BAD REQUEST";
        }
        return reply;
    }

    /**
     * This method checks if the given number is an integer or not.
     *
     * @return true, if the number is an integer.Otherwise, false.
     */
    private boolean isInteger(double number) {
        return number % 1 == 0;
    }


    /**
     * This method checks the correctness of the API request.
     *
     * @param str the given command
     * @return true, if the given command is HOLIDAYS or LATEST NEWS. Otherwise, false.
     */
    private boolean checkAPI_Request(String str) {
        String[] str_parts = str.split(" ");
        return ((str_parts[0].equals("HOLIDAYS") && isInteger(Integer.parseInt(str_parts[1]))) ||
                (str_parts[0].equals("LATEST") && str_parts[1].equals("NEWS")) || str_parts[0].equals("LIST"));
    }

    /**
     * This method makes an API request and gives back the received information.
     *
     * @param str the given command
     * @return the information from the corresponding API.
     */
    private String getAPI(String str) {
        String[] str_parts = str.split(" ");

        switch (str_parts[0]) {

            //------------------------------------ HOLIDAYS API ------------------------------------//
            case "HOLIDAYS" -> {
                URL url1 = null;
                HttpURLConnection conn1 = null;
                int responseCode1 = 0;
                StringBuilder informationString = new StringBuilder();
                try {
                    url1 = new URL("https://feiertage-api.de/api/?jahr=<Jahr>&nur_land=NW");
                } catch (MalformedURLException ignored) {
                }
                try {
                    conn1 = (HttpURLConnection) (url1 != null ? url1.openConnection() : null);
                } catch (IOException ignored) {
                }
                try {
                    if (conn1 != null) {
                        conn1.setRequestMethod("GET");
                    }
                } catch (ProtocolException ignored) {
                }
                try {
                    if (conn1 != null) {
                        conn1.connect();
                    }
                } catch (IOException ignored) {
                }

                //Checks if connection is made
                try {
                    responseCode1 = conn1 != null ? conn1.getResponseCode() : 0;
                } catch (IOException ignored) {
                }
                if (responseCode1 != 200) {
                    return "ERROR 404 NOT FOUND";
                } else {
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(url1.openStream());
                    } catch (IOException ignored) {
                    }

                    while (scanner != null && scanner.hasNext()) {
                        informationString.append(scanner.nextLine());
                    }
                    //Close the scanner
                    if (scanner != null) {
                        scanner.close();
                    }
                }
                return informationString.toString();
            }

            //------------------------------------ LATEST NEWS API ------------------------------------//
            case "LATEST" -> {
                URL url2 = null;
                HttpURLConnection conn2 = null;
                int responseCode2 = 0;
                StringBuilder informationString2 = new StringBuilder();
                try {
                    url2 = new URL("https://www.tagesschau.de/api2/");
                } catch (MalformedURLException ignored) {
                }
                try {
                    conn2 = (HttpURLConnection) (url2 != null ? url2.openConnection() : null);
                } catch (IOException ignored) {
                }
                try {
                    if (conn2 != null) {
                        conn2.setRequestMethod("GET");
                    }
                } catch (ProtocolException ignored) {
                }
                try {
                    if (conn2 != null) {
                        conn2.connect();
                    }
                } catch (IOException ignored) {
                }

                //Checks if connection is made
                try {
                    responseCode2 = conn2 != null ? conn2.getResponseCode() : 0;
                } catch (IOException ignored) {
                }
                if (responseCode2 != 200) {
                    return "ERROR 404 NOT FOUND";
                } else {
                    Scanner scanner2 = null;
                    try {
                        scanner2 = new Scanner(url2.openStream());
                    } catch (IOException ignored) {
                    }

                    while (scanner2 != null && scanner2.hasNext()) {
                        informationString2.append(scanner2.nextLine());
                    }
                    //Close the scanner
                    if (scanner2 != null) {
                        scanner2.close();
                    }
                }
                return informationString2.toString();
            }

            //------------------------------------ LIST UNIVERSITIES API ------------------------------------//
            case "LIST" -> {
                if (str_parts[1].isEmpty()) {
                    return "ERROR 400 BAD REQUEST";
                }

                // the given country
                String country = getCountry(str);

                // List of all universities in the given country
                URL url3 = null;
                HttpURLConnection conn3 = null;
                int responseCode3 = 0;
                StringBuilder informationString3 = new StringBuilder();
                try {
                    url3 = new URL("http://universities.hipolabs.com/search?country=" + country);
                } catch (MalformedURLException ignored) {
                }
                try {
                    conn3 = (HttpURLConnection) (url3 != null ? url3.openConnection() : null);
                } catch (IOException ignored) {
                }
                try {
                    if (conn3 != null) {
                        conn3.setRequestMethod("GET");
                    }
                } catch (ProtocolException ignored) {
                }
                try {
                    if (conn3 != null) {
                        conn3.connect();
                    }
                } catch (IOException ignored) {
                }

                //Checks if connection is made
                try {
                    responseCode3 = conn3 != null ? conn3.getResponseCode() : 0;
                } catch (IOException ignored) {
                }
                if (responseCode3 != 200) {
                    return "ERROR 404 NOT FOUND";
                } else {
                    Scanner scanner2 = null;
                    try {
                        scanner2 = new Scanner(url3.openStream());
                    } catch (IOException ignored) {
                    }

                    while (scanner2 != null && scanner2.hasNext()) {
                        informationString3.append(scanner2.nextLine());
                    }
                    //Close the scanner
                    if (scanner2 != null) {
                        scanner2.close();
                    }
                }
                return informationString3.toString();
            }
            default -> {
                return "ERROR 500 INTERNAL SERVER ERROR";
            }
        }
    }

    /**
     * This method gives the country name passed with the LIST-command.
     *
     * @param str the given command
     * @return a string containing the name of the given country to be given to the http-URL.
     */
    private String getCountry(String str) {
        String[] str_parts = str.split(" ");
        // a single-name country like Germany, France, ...etc.
        if (str_parts.length == 2) {
            return str_parts[1];
        } else {
            // a multiple-name country like the United States, ...etc.
            StringBuilder country_name = new StringBuilder();
            for (int i = 1; i < str_parts.length; i++) {
                country_name.append(str_parts[i]).append("+");
            }
            // returns the whole String ignoring the last "+"-sign
            return country_name.substring(0, country_name.length() - 1);
        }
    }

    /**
     * This method checks the correctness of the mathematical operation command.
     *
     * @param str the given command
     * @return true, if the command is ADD, SUB, MUL or DIV.
     */
    private boolean checkOperation(String str) {
        String[] str_parts = str.split(" ");
        return str_parts[0].equals("ADD") || str_parts[0].equals("MUL") ||
                str_parts[0].equals("SUB") || str_parts[0].equals("DIV");
    }

    /**
     * This method calculates the basic mathematical operation: Addition, Subtraction, Division, Modulo and Multiplication.
     *
     * @param str the given command
     * @return the result of the operation.
     */
    private String getOperation(String str) {
        String[] str_parts = str.split(" ");

        // checks the number of parameters
        if (str_parts.length != 3) {
            return "ERROR 400 BAD REQUEST";
        } else {
            switch (str_parts[0]) {
                case "ADD" -> {
                    return Integer.toString(Math.addExact(Integer.parseInt(str_parts[1]),
                            Integer.parseInt(str_parts[2])));
                }
                case "SUB" -> {
                    return Integer.toString(Math.subtractExact(Integer.parseInt(str_parts[1]),
                            Integer.parseInt(str_parts[2])));
                }
                case "MUL" -> {
                    return Integer.toString(Math.multiplyExact(Integer.parseInt(str_parts[1]),
                            Integer.parseInt(str_parts[2])));
                }
                case "DIV" -> {
                    return Integer.toString(Math.divideExact(Integer.parseInt(str_parts[1]),
                            Integer.parseInt(str_parts[2])));
                }
                default -> {
                    return "ERROR 400 BAD REQUEST";
                }
            }
        }
    }

}
