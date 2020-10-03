package ru.nsu.g.akononov.fileReceiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static int portNumber;

    public static void main(String[] args) {
        try {
            portNumber = parseArgs(args);
        } catch (IllegalArgumentException exception) {
            printHint();
            return;
        }
        createServer();
    }

    private static void createServer() {
        try (var serverSocket = new ServerSocket(portNumber)){
            System.out.println("Server " + serverSocket.getInetAddress() + " is running.");

            while (true){
                Socket clientSocket = serverSocket.accept();
                final var thread = new Thread(() -> {
                    try (final var receiver = new Receiver(clientSocket)) {
                        receiver.run();
                    }
                });
                thread.start();
                System.out.println("Client " + clientSocket.getInetAddress() + " is connected");
            }

        } catch (IOException e) {
            System.err.println("Cannot create server: " + e.getMessage());
        }
    }

    private static void printHint() {
        System.err.println("Please set the correct server port");
        System.err.println("Expected input: [PORT_NUMBER]");
    }

    private static Integer parseArgs(String[] args) throws IllegalArgumentException {
        int portNumber;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        } else {
            throw new IllegalArgumentException();
        }
        return portNumber;
    }
}