package ru.nsu.g.akononov.fileReceiver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Receiver extends Thread {
    private static final int TIMEOUT = 10000;
    private static final int PACKET_SIZE = 512;

    public static final int BUFFER_SIZE = 4 * 1024; //min 256
    public static final int ACK_CODE = 729;
    public static final int ERR_CODE = 814;

    private final List<Integer> checkSums = new ArrayList<>();
    private final UploadingFile file = new UploadingFile();
    private SpeedChecker speedChecker;

    private Socket mainSocket;
    private SocketStreams socket;

    public Receiver(Socket socket) {
        try {
            mainSocket = socket;

            socket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (SocketStreams socketStreams = new SocketStreams(mainSocket)){
            socket = socketStreams;

            String fileName = readFileName();
            long fileSize = readFileSize();
            file.setExpectedSize(fileSize);
            receiveFile(fileName);
            sendAcknowledgment();
            System.out.println("[" + file.getName() + "] " + "File was downloaded successfully!");
        }
        catch (IOException | RuntimeException e) {
            System.err.println(socket.getSocket().getInetAddress());
            if (e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void sendAcknowledgment() throws IOException {
        int clientFileHashCode = socket.readInt();
        int currentFileHashCode = Arrays.hashCode(checkSums.toArray());
        if (clientFileHashCode == currentFileHashCode) {
            socket.writeInt(ACK_CODE);
        } else {
            socket.writeInt(ERR_CODE);
            throw new RuntimeException("[" + file.getName() + "] " + "Incorrect file data checksum!" +
                    currentFileHashCode + "/" + clientFileHashCode + "\n");
        }
    }

    private void receiveFile(String fileName) throws IOException {
        file.create(fileName);
        speedChecker = new SpeedChecker(file.getName());

        while (file.getRemainingByteCount() >= BUFFER_SIZE) {
            readFilePart(BUFFER_SIZE);
        }
        if (!file.isReady()) {
            readFilePart((int) file.getRemainingByteCount());
        }
    }

    private void readFilePart(int partByteCount) throws IOException {
        byte[] fileBuffer = new byte[partByteCount];
        int bufferOffset = 0;
        int remainingPartSize = partByteCount;

        while (remainingPartSize >= PACKET_SIZE) {
            byte[] buffer = new byte[PACKET_SIZE];
            int readByteCount = socket.read(buffer);
            System.arraycopy(buffer, 0, fileBuffer, bufferOffset, readByteCount);
            bufferOffset += readByteCount;
            remainingPartSize -= readByteCount;

            speedChecker.checkSpeed(file.getCurrentSize() + partByteCount - remainingPartSize);
        }
        while (remainingPartSize != 0) {
            byte[] buffer = new byte[remainingPartSize];
            int readByteCount = socket.read(buffer);
            System.arraycopy(buffer, 0, fileBuffer, bufferOffset, readByteCount);
            remainingPartSize -= readByteCount;
            bufferOffset += readByteCount;

            speedChecker.checkSpeed(file.getCurrentSize() + partByteCount - remainingPartSize);
        }

        file.write(fileBuffer, 0, partByteCount);
        checkSums.add(Arrays.hashCode(fileBuffer));
    }



    private String readFileName() throws IOException {
        int nameLength = socket.readInt();

        byte[] fileName = new byte[nameLength];
        socket.read(fileName);

        int currentHashCode = Arrays.hashCode(fileName);
        int clientHashCode = socket.readInt();

        if (currentHashCode == clientHashCode) {
            socket.writeInt(ACK_CODE);
            return new String(fileName, StandardCharsets.UTF_8);
        } else {
            socket.writeInt(ERR_CODE);
            throw new RuntimeException("Incorrect file name checksum!");
        }
    }

    private long readFileSize() throws IOException {
        return socket.readLong();
    }
}