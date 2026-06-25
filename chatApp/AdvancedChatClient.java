import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AdvancedChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Change to Server IP if running over a network
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            out.println(name); // Send username to the server first

            System.out.println("--- Connected to the Chat Room (Type '/quit' to exit) ---");

            // Start a background thread to continuously listen for incoming server messages
            Thread readThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readThread.start();

            // Main thread handles capturing user inputs and sending them out
            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                out.println(userInput);
                if (userInput.equalsIgnoreCase("/quit")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Client Error: Could not connect to server. " + e.getMessage());
        }
    }
}