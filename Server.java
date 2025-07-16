import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 3332;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");

            ClientHandler handler = new ClientHandler(socket, clientHandlers);
            clientHandlers.add(handler);
            new Thread(handler).start();
        }
    }
}
