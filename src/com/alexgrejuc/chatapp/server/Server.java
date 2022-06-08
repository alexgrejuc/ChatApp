package com.alexgrejuc.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The multithreaded server component of a chat application.
 */
public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Listens for client connections and disconnections on the configured port.
     * Handles each client in its own client handler thread.
     */
    public void startServer() {
        System.out.println("The server is running on port " + serverSocket.getLocalPort());

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected.");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException ioe) {
            System.out.println("Error accepting new client: ");
            ioe.printStackTrace();
            stopServer();
        }
    }

    /**
     * Stops handling clients and closes any resources used by the server.
     */
    private void stopServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ioe) {
            System.err.println("Error stopping server: ");
            ioe.printStackTrace();
        }
    }

    /**
     * Starts a server that listens for clients at a default port.
     *
     * @param args currently no command line arguments are supported
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int port = 7777;
        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
