package fileSender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Sender{
    public Sender(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] buffer = ByteBuffer.allocate(4).putInt(7812453).array();
        out.write(buffer, 0, 4);
    }
}
