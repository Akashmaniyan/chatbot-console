import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3332;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_IP, PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Thread to read messages from server
        new Thread(() -> {
            String msgFromServer;
            try {
                while ((msgFromServer = in.readLine()) != null) {
                    System.out.println(msgFromServer);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        }).start();

        // Main thread for sending user messages
        String msg;
        while ((msg = userInput.readLine()) != null) {
            out.println(msg);
        }
    }
}

