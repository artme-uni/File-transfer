package ru.nsu.g.akononov.fileSender;

import ru.nsu.g.akononov.fileReceiver.Receiver;
import ru.nsu.g.akononov.fileReceiver.SpeedChecker;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sender {

    private OutputStream socketOutput;
    private DataOutputStream out;
    private InputStream socketInput;
    private DataInputStream in;

    private File file;

    private final int BUFFER_SIZE;
    private final int ACK_CODE;

    public Sender(Socket socket, File file) {
        BUFFER_SIZE = Receiver.BUFFER_SIZE;
        ACK_CODE = Receiver.ACK_CODE;

        try {
            socketOutput = socket.getOutputStream();
            socketInput = socket.getInputStream();

            this.file = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send() {
        try (DataInputStream dataInputStream = new DataInputStream(socketInput);
             DataOutputStream dataOutputStream = new DataOutputStream(socketOutput)) {
            in = dataInputStream;
            out = dataOutputStream;

            printFileInfo();

            sendNameLength();
            int nameHashCode = sendFileName();
            checkAcknowledgment(nameHashCode);

            sendFileSize();
            int fileHashCode = sendFile();
            checkAcknowledgment(fileHashCode);
            System.out.println("File " + file.getName() + " was uploaded successfully!");

        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (RuntimeException exception){
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void printFileInfo() {
        System.out.println("File information");
        System.out.println("\t- Path: " + file.getPath());
        System.out.println("\t- Size: " + SpeedChecker.readableByteCount(file.length()));
    }

    private void sendNameLength() throws IOException {
        int fileLength = file.getName().getBytes().length;
        out.writeInt(fileLength);
    }

    private int sendFileName() throws IOException {
        byte[] buffer = file.getName().getBytes(StandardCharsets.UTF_8);
        out.write(buffer);
        return Arrays.hashCode(buffer);
    }

    private void sendFileSize() throws IOException {
        long fileSize = file.length();
        out.writeLong(fileSize);
    }

    private int sendFile() throws IOException {
        List<Integer> checkSums = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(file);

        long remainingByteCount = file.length();
        while (remainingByteCount >= BUFFER_SIZE) {
            int hashCode = writeFilePart(BUFFER_SIZE, fileInputStream);
            checkSums.add(hashCode);
            remainingByteCount -= BUFFER_SIZE;
        }
        if (remainingByteCount != 0) {
            int hashCode = writeFilePart((int) remainingByteCount, fileInputStream);
            checkSums.add(hashCode);
        }

        return Arrays.hashCode(checkSums.toArray());
    }

    private int writeFilePart(int byteCount, FileInputStream file) throws IOException {
        byte[] buffer = new byte[byteCount];
        file.read(buffer);
        out.write(buffer);
        return Arrays.hashCode(buffer);
    }

    private void checkAcknowledgment(int hashCode) throws IOException {
        out.writeInt(hashCode);
        int code = in.readInt();
        if(code != ACK_CODE){
            throw new RuntimeException("Incorrect checksum!");
        }
    }
}
