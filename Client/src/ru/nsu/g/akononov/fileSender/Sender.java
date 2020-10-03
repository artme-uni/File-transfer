package ru.nsu.g.akononov.fileSender;

import ru.nsu.g.akononov.fileReceiver.Receiver;
import ru.nsu.g.akononov.fileReceiver.SpeedTester;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Sender implements AutoCloseable {
    private final static int TIMEOUT = 10000;

    private DataOutputStream out;
    private DataInputStream in;

    private File file;

    public Sender(Socket socket, File file) {
        try {
            socket.setSoTimeout(TIMEOUT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.file = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send() throws IOException, RuntimeException {
        printFileInfo();

        sendFileName();
        sendFileSize();
        sendFile();

        if (in.readInt() == Receiver.ACK_CODE) {
            System.out.println("File " + file.getName() + " was uploaded successfully!");
        } else {
            System.out.println("File " + file.getName() + " was NOT uploaded successfully!");
        }
    }

    private void printFileInfo() {
        System.out.println("File information");
        System.out.println("\t- Path: " + file.getPath());
        System.out.println("\t- Size: " + SpeedTester.readableByteCount(file.length()));
    }


    private void sendFileName() throws IOException {
        int fileLength = file.getName().getBytes().length;
        out.writeInt(fileLength);
        byte[] buffer = file.getName().getBytes(StandardCharsets.UTF_8);
        out.write(buffer);
    }

    private void sendFileSize() throws IOException {
        long fileSize = file.length();
        out.writeLong(fileSize);
    }

    private void sendFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        long remainingByteCount = file.length();
        while (remainingByteCount != 0) {
            int BUFFER_SIZE = Receiver.PACKET_SIZE;
            int partSize = BUFFER_SIZE;
            if (remainingByteCount < BUFFER_SIZE) {
                partSize = (int) remainingByteCount;
            }
            writeFilePart(partSize, fileInputStream);
            remainingByteCount -= partSize;
        }
        fileInputStream.close();
    }

    private void writeFilePart(int byteCount, FileInputStream file) throws IOException {
        byte[] buffer = new byte[byteCount];
        int readByteCount = file.read(buffer);
        out.write(Arrays.copyOf(buffer, readByteCount));
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
