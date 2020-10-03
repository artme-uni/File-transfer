package ru.nsu.g.akononov.fileSender;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    private static int port;
    private static String address;
    private static File file;

    public static void main(String[] args) {
        try {
            parseArgs(args);
        }catch (IllegalArgumentException exception){
            System.err.println("Bad arguments, try again");
            printHint(exception);
            return;
        }
        sendFile();
    }

    public static void sendFile(){
        try (var socket = new Socket(InetAddress.getByName(address), port);
             var sender = new Sender(socket, file)) {
            sender.send();
        } catch (IOException e) {
            System.err.println("Cannot connect to the server " + address + ":" + port);
            System.err.println("Please try again later (" + e.getMessage() + ")");
        } catch (IllegalArgumentException e) {
            System.err.println("Illegal arguments: " + e.getMessage());
        }
    }

    private static void printHint(Exception exception) {
        if(exception.getMessage() != null){
            System.err.println("Hint: " + exception.getMessage());
        } else {
            System.err.println("Expected keys:\n" +
                    "\t-addr [SERVER_ADDRESS] - to set server address\n" +
                    "\t-port [PORT_NUMBER] - to set server port");
        }
    }

    private static void parseArgs(String[] args) throws IllegalArgumentException {
        if (args.length == 5) {
            List<String> arguments = new ArrayList<>(Arrays.asList(args));

            for (int i = 0; i < arguments.size(); ) {
                if (arguments.get(i).equals("-port")) {
                    port = Integer.parseInt(arguments.get(i + 1));
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
            file = new File(arguments.get(0));
            if(!file.isFile()){
                throw new IllegalArgumentException(file.getName() + " is not a file");
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}