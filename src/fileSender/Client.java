package fileSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    private static int port;
    private static String address;
    private static String fileName;

    private final static int MIN_PORT = 1024;
    private final static int MAX_PORT = 65535;

    public static void main(String[] args) {
        try {
            parseArgs(args);
        }catch (IllegalArgumentException exception){
            if(exception.getMessage() != null){
                System.out.println(exception.getMessage());
            } else {
                printHint();
            }
            return;
        }

        try {
            Socket socket = new Socket(InetAddress.getByName(address), port);
            Sender sender = new Sender(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHint() {
        System.out.println("Expected keys:\n" +
                "\t-addr [SERVER_ADDRESS] - to set server address\n" +
                "\t-port [PORT_NUMBER] - to set server port");
    }

    private static void parseArgs(String[] args) throws IllegalArgumentException {
        if (args.length == 4) {
            List<String> arguments = new ArrayList<>(Arrays.asList(args));

            for (int i = 0; i < arguments.size(); ) {
                if (arguments.get(i).equals("-port")) {
                    port = Integer.parseInt(arguments.get(i + 1));
                    if (port > MAX_PORT || port < MIN_PORT) {
                        throw new IllegalArgumentException("Port must be from " + MIN_PORT + " to " + MAX_PORT);
                    }
                    arguments.remove(i + 1);
                    arguments.remove(i);
                    continue;
                }
                if (arguments.get(i).equals("-addr")) {
                    address = arguments.get(i + 1);
                    arguments.remove(i + 1);
                    arguments.remove(i);
                    continue;
                }
                i++;
            }
            if (arguments.size() != 1) {
                throw new IllegalArgumentException();
            }
            fileName = arguments.get(0);
        } else {
            throw new IllegalArgumentException();
        }
    }
}