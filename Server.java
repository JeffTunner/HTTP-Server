import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening at port: " + port);
            Socket socket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            System.out.println("Client Connected!");
            String message = inputStream.readUTF();
            System.out.println("Message: " +message);
        } catch (Exception e) {
            System.out.println("Exception: " +e);
        }
    }
}
