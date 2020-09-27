package fileReceiver;

import java.io.*;
import java.net.Socket;

public class SocketStreams implements AutoCloseable{
    private final DataOutputStream out;
    private final DataInputStream in;

    Socket socket;

    public SocketStreams(Socket socket) throws IOException {
        this.socket = socket;

        OutputStream socketOutput = socket.getOutputStream();
        InputStream socketInput = socket.getInputStream();
        in = new DataInputStream(socketInput);
        out = new DataOutputStream(socketOutput);
    }

    public Socket getSocket() {
        return socket;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return in.read(buffer, offset, length);
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public int read(byte[] buffer) throws IOException {
        return in.read(buffer);
    }

    public void writeInt(int value) throws IOException {
        out.writeInt(value);
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}