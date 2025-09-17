import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerUI {
    private static final int PORT = 3332;
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ServerUI() {
        // --- Setup UI ---
        frame = new JFrame("Chat Server");
        chatArea = new JTextArea();
        inputField = new JTextField();
        sendButton = new JButton("Send");

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start server thread
        new Thread(this::startServer).start();

        // Event handling
        sendButton.addActionListener(e -> sendMessageToAll());
        inputField.addActionListener(e -> sendMessageToAll());
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            chatArea.append("Server started on port " + PORT + "\n");

            while (true) {
                Socket socket = serverSocket.accept();
                chatArea.append("New client connected\n");

                ClientHandler handler = new ClientHandler(socket, clientHandlers, this);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            chatArea.append("Server error: " + e.getMessage() + "\n");
        }
    }

    private void sendMessageToAll() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            broadcast("Server: " + message);
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
        }
    }

    public void broadcast(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }

    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerUI::new);
    }
}

// --- ClientHandler ---
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clientHandlers;
    private ServerUI serverUI;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers, ServerUI serverUI) {
        this.socket = socket;
        this.clientHandlers = clientHandlers;
        this.serverUI = serverUI;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            serverUI.showMessage("Error setting up client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {
                serverUI.showMessage("Client: " + msg);
                broadcast("Client: " + msg);
            }
        } catch (IOException e) {
            serverUI.showMessage("Client disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            clientHandlers.remove(this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void broadcast(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(message);
            }
        }
    }
}
