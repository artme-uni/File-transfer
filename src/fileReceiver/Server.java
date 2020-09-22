package fileReceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final static int MIN_PORT = 1024;
    private final static int MAX_PORT = 65535;

    public static void main(String[] args) {

        int portNumber;
        try {
            portNumber = parseArgs(args);
        } catch (IllegalArgumentException exception) {
            if(exception.getMessage() != null){
                System.out.println(exception.getMessage());
            } else {
                printHint();
            }
            return;
        }

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server " + InetAddress.getLocalHost() + " is running.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client " + clientSocket.getInetAddress() + " is connected");
                Receiver receiver = new Receiver(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHint() {
        System.out.println("Expected keys:\n" +
                "\t-port [PORT_NUMBER] - to set server port");
    }

    private static Integer parseArgs(String[] args) throws IllegalArgumentException {
        if (args.length == 2) {
            if (!args[0].toLowerCase().equals("-port")) {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
        int portNumber;
        portNumber = Integer.parseInt(args[1]);

        if (portNumber > MAX_PORT || portNumber < MIN_PORT) {
            throw new IllegalArgumentException("Port must be from " + MIN_PORT + " to " + MAX_PORT);
        }
        return portNumber;
    }
}