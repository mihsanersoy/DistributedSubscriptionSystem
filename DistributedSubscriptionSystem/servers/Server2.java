import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server2 {
    private static final int SERVER_COMM_PORT = 12351;
    private static final int ADMIN_PORT = 1123;
    private static final int CLIENT_PORT = 1133;

    private final Map<String, String> clientData = new HashMap<>();
    private static final String SERVER1_HOST = "localhost";
    private static final int SERVER1_PORT = 12353;

    public static void main(String[] args) {
        Server2 server = new Server2();

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
        int[] otherServerPorts = {12353, 12356};
        for (int port : otherServerPorts) {
            try {
                Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                String messageToSend = "Hello from Server2!";
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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminRequest(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            System.out.println("Admin client connected.");
            out.writeObject("Server2 is ready for admin commands.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            String message = (String) in.readObject();
            System.out.println("Received from client: " + message);


            String clientAddress = socket.getInetAddress().toString();
            synchronized (clientData) {
                clientData.put(clientAddress, message);
                System.out.println("Client data saved locally: " + clientAddress + " -> " + message);
            }


            sendClientDataToServer1(clientAddress, message);

            out.writeObject("ACK: Message received by Server2.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendClientDataToServer1(String clientAddress, String message) {
        try (Socket socket = new Socket(SERVER1_HOST, SERVER1_PORT); ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            String dataToSend = "Client from " + clientAddress + " sent: " + message;
            out.writeObject(dataToSend);
            System.out.println("Client data forwarded to Server1: " + dataToSend);

        } catch (IOException e) {
            System.out.println("Could not send client data to Server1.");
            e.printStackTrace();
        }
    }
}
