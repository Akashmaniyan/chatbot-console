import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3332;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client() {
        // --- Setup UI ---
        frame = new JFrame("Chat Client");
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

        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // --- Connect to server ---
        try {
            socket = new Socket(SERVER_IP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append("Connected to server.\n");

            // Thread for receiving messages
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server.\n");
        }

        // --- Event handling ---
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && out != null) {
            out.println(msg); // send to server
            chatArea.append("You: " + msg + "\n");
            inputField.setText("");
        }
    }

    private void receiveMessages() {
        String msgFromServer;
        try {
            while ((msgFromServer = in.readLine()) != null) {
                chatArea.append("Server: " + msgFromServer + "\n");
            }
        } catch (IOException e) {
            chatArea.append("Disconnected from server.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
