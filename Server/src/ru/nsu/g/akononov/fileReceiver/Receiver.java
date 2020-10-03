package ru.nsu.g.akononov.fileReceiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Receiver implements AutoCloseable, Runnable {
    private static final int TIMEOUT = 10000;

    public static final int PACKET_SIZE = 512;
    public static final int ACK_CODE = 727;
    public static final int ERR_CODE = 728;

    private final UploadingFile file = new UploadingFile();

    private final DataOutputStream out;
    private final DataInputStream in;
    private final Socket socket;


    public Receiver(Socket socket) {
        try {
            this.socket = socket;
            socket.setSoTimeout(TIMEOUT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            String fileName = readFileName();
            long fileSize = in.readLong();
            file.setExpectedSize(fileSize);
            receiveFile(fileName);

            if(file.isCorrect()){
                out.writeInt(ACK_CODE);
                System.out.println("[" + file.getName() + "] " + "File was downloaded successfully!");
            } else {
                out.writeInt(ERR_CODE);
                System.out.println("[" + file.getName() + "] " + "File was NOT downloaded successfully!");
            }
        } catch (IOException | RuntimeException e) {
            System.err.println(socket.getInetAddress() + (e.getMessage() != null ? " : " + e.getMessage() : ""));
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private String readFileName() throws IOException {
        int nameLength = in.readInt();
        byte[] fileName = new byte[nameLength];
        int readByteCount = in.read(fileName);
        return new String(Arrays.copyOf(fileName, readByteCount), StandardCharsets.UTF_8);
    }

    private void receiveFile(String fileName) throws IOException {
        file.create(fileName);
        SpeedTester speedTester = new SpeedTester(file);

        while (!file.isReady()) {
            int partSize = PACKET_SIZE;
            if (file.getRemainingByteCount() < PACKET_SIZE) {
                partSize = (int) file.getRemainingByteCount();
            }
            byte[] filePart = readFilePart(partSize);
            file.write(filePart);
            speedTester.check();
        }
    }

    private byte[] readFilePart(int partByteCount) throws IOException {
        byte[] buffer = new byte[partByteCount];
        int readByteCount = in.read(buffer);
        return Arrays.copyOf(buffer, readByteCount);
    }

    @Override
    public void close() {
        try {
            file.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}