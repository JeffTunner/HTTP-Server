import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        int port = 8080;
        try {
            Socket s = new Socket("localhost", port);
            DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
            outputStream.writeUTF("Hello, World!");
            outputStream.flush();
            outputStream.close();
            s.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
