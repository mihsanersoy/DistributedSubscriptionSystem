import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server3 {
    private static final int SERVER_COMM_PORT = 12356;
    private static final int ADMIN_PORT = 1124;
    private static final int CLIENT_PORT = 1133;

    private final Map<String, String> clientData = new HashMap<>();

    public static void main(String[] args) {
        Server3 server = new Server3();

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
        int[] otherServerPorts = {12351, 12353};
        for (int port : otherServerPorts) {
            try {
                Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                String messageToSend = "Hello from Server3!";
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
                String[] parts = message.split(" -> ");
                if (parts.length == 2) {
                    clientData.put(parts[0], parts[1]);
                    System.out.println("Data saved from another server: " + parts[0] + " -> " + parts[1]);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminRequest(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            System.out.println("Admin client connected.");
            out.writeObject("Server3 is ready for admin commands.");
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
                System.out.println("Client data saved: " + clientAddress + " -> " + message);
            }

            sendDataToOtherServers(clientAddress + " -> " + message);

            out.writeObject("ACK: Message received by Server3.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendDataToOtherServers(String data) {
        int[] otherServerPorts = {12351, 12353};
        for (int port : otherServerPorts) {
            try {
                Socket socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(data);
                System.out.println("Sent to Server on port: " + port + " : " + data);

                socket.close();
            } catch (IOException e) {
                System.out.println("Could not send data to server on port: " + port);
                e.printStackTrace();
            }
        }
    }
}
