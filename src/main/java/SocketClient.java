import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {
    Socket socket = null;
    OutputStream output = null;

    public SocketClient(int port) {
        try {
            socket = new Socket("localhost", port);
            output = socket.getOutputStream();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void send(byte[] data) {
        try {
            output.write(data);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void close() {
        try {
            output.close();
            socket.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
