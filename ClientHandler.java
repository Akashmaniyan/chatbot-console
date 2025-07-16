import java.io.*;
import java.net.*;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clientHandlers;
    private String name;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers) {
        this.socket = socket;
        this.clientHandlers = clientHandlers;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Enter your name:");
            this.name = in.readLine();
            broadcast(name + " joined the chat.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {
                broadcast("[" + name + "]: " + msg);
            }
        } catch (IOException e) {
            System.out.println(name + " disconnected.");
        } finally {
            try {
                socket.close();
                clientHandlers.remove(this);
                broadcast(name + " left the chat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.out.println(message);
        }
    }
}
