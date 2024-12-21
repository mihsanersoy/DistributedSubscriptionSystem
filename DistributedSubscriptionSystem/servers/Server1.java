import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server1 {
    private static final int SERVER_COMM_PORT = 12353;
    private static final int ADMIN_PORT = 1122;
    private static final int CLIENT_PORT = 1133;

    private final Map<String, String> clientData = new HashMap<>();

    public static void main(String[] args) {
        Server1 server = new Server1();

        new Thread(server::startServerCommunication).start();
        new Thread(server::startAdminListener).start();
        new Thread(server::startClientListener).start();

        server.connectToOtherServers();
    }

    public void startServerCommunication() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_COMM_PORT)) {
            System.out.println("Server communication port running on: " + SERVER_COMM_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleServerRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAdminListener() {
        try (ServerSocket serverSocket = new ServerSocket(ADMIN_PORT)) {
            System.out.println("Admin port running on: " + ADMIN_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleAdminRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClientListener() {
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {
            System.out.println("Client port running on: " + CLIENT_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClientRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToOtherServers() {
        int[] otherServerPorts = {12351, 12356};
        for (int port : otherServerPorts) {
            try {
                Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                String messageToSend = "Hello from Server1!";
                out.writeObject(messageToSend);
                System.out.println("Sent to Server on port: " + port + " : " + messageToSend);

                String response = (String) in.readObject();
                System.out.println("Received from Server on port " + port + ": " + response);

                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Could not connect to server on port: " + port);
                e.printStackTrace();
            }
        }
    }

    private void handleServerRequest(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            String message = (String) in.readObject();
            System.out.println("Received from another server: " + message);

            synchronized (clientData) {
                String[] parts = message.split("->"); // Gelen veriyi "key -> value" formatında ayırıyoruz
                if (parts.length == 2) {
                    String clientAddress = parts[0].trim();
                    String clientMessage = parts[1].trim();
                    clientData.put(clientAddress, clientMessage);
                    System.out.println("Client data updated from server: " + clientAddress + " -> " + clientMessage);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminRequest(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            System.out.println("Admin client connected.");
            out.writeObject("Server1 is ready for admin commands.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            String message = (String) in.readObject();
            System.out.println("Received from client: " + message);

            String clientAddress = socket.getInetAddress().toString();

            synchronized (clientData) {
                clientData.put(clientAddress, message);
                System.out.println("Client data saved: " + clientAddress + " -> " + message);
            }

            out.writeObject("ACK: Message received by Server1.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void printClientData() {
        System.out.println("Current client data:");
        synchronized (clientData) {
            clientData.forEach((key, value) -> System.out.println(key + " -> " + value));
        }
    }
}
