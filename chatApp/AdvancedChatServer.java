import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdvancedChatServer {
    private static final int PORT = 12345;
    // Thread-safe set to keep track of all active client output streams
    private static final Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server started...");
        // Using a thread pool to manage multiple client connections efficiently
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());
                
                // Submit the client handling task to the thread pool
                pool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    // Broadcasts a message to all connected clients
    public static void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    // Inner class to handle individual client communication loops
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // The first message sent by the client is treated as their name
                clientName = in.readLine();
                if (clientName == null || clientName.trim().isEmpty()) {
                    return;
                }

                // Add this client's writer to the broadcast pool
                clientWriters.add(out);
                broadcast("[SERVER] " + clientName + " has joined the chat room!");
                System.out.println(clientName + " has joined.");

                // Continuous message reading loop
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    broadcast(clientName + ": " + message);
                }
            } catch (IOException e) {
                System.err.println("Error handling client " + clientName + ": " + e.getMessage());
            } finally {
                // Cleanup when client disconnects
                if (out != null) {
                    clientWriters.remove(out);
                }
                if (clientName != null) {
                    broadcast("[SERVER] " + clientName + " has left the chat.");
                    System.out.println(clientName + " disconnected.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close socket: " + e.getMessage());
                }
            }
        }
    }
}