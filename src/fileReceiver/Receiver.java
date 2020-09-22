package fileReceiver;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Receiver extends Thread{
    private static final int TIMEOUT = 10000;

    private Socket socket;
    private InputStream input;

    public Receiver(Socket socket) {
        this.socket = socket;
        try {
            input = socket.getInputStream();
            socket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        try {
            int fileNameLength = readFileNameLength();
            System.out.println(fileNameLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int readFileNameLength() throws IOException {
        int fileNameLength;
        try(DataInputStream in = new DataInputStream(input)){
            fileNameLength = in.readInt();
        }
        return fileNameLength;
    }

}
