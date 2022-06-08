package com.alexgrejuc.chatapp.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles an individual client's connection, disconnection, and messaging.
 */
public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;
    private BufferedReader messageReader;
    private BufferedWriter messageWriter;
    private String clientUsername;

    /**
     * Creates a client handler, gets the client's username, and broadcasts that they have entered the chat.
     * @param socket
     */
    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.messageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.messageWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // A client sends their username when first logging in
            this.clientUsername = messageReader.readLine();

            clientHandlers.add(this);
            broadcastServerMessage(clientUsername + " has entered the chat.");
        } catch (IOException ioe) {
            System.err.println("Error creating client handler:");
            ioe.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Listens for a message from a client and broadcasts it to the other clients.
     */
    @Override
    public void run() {
        String messageFromClient;
        boolean clientConnected = true;

        // isClosed checks that socket has not been closed from the server side (e.g. due to exception)
        // clientConnected checks that the socket has not been closed from the client side
        while (!socket.isClosed() && clientConnected) {
            try {
                messageFromClient = messageReader.readLine();

                // null means the end of stream was reached, i.e. the client disconnected abruptly
                clientConnected = messageFromClient != null;

                if (clientConnected) {
                    broadcastMessage(messageFromClient);
                }
                else {
                    closeAllResources();
                }
            } catch (IOException ioe) {
                System.err.println("Error reading message from client: ");
                ioe.printStackTrace();
                closeAllResources();
            }
        }
    }

    /**
     * Broadcasts a message to all clients except this one.
     * @param message
     * @param senderName The name of the sender, which is either the server or client username.
     */
    public void broadcastMessage(String message, String senderName) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler != this) {
                    clientHandler.messageWriter.write(senderName + ": " + message);
                    clientHandler.messageWriter.newLine();
                    clientHandler.messageWriter.flush();
                }
            } catch (IOException ioe) {
                System.err.println("Error broadcasting message: ");
                ioe.printStackTrace();
                closeAllResources();
            }
        }
    }

    /**
     * Broadcasts a message from this client.
     * @param message
     */
    public void broadcastMessage(String message) {
        broadcastMessage(message, clientUsername);
    }

    /**
     * Broadcasts a message from the server to all the clients.
     * @param message
     */
    public void broadcastServerMessage(String message) {
        broadcastMessage(message, "SERVER");
    }

    /**
     * Removes clientHandler from clientHandlers to ensure no future messages are sent to it.
     */
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastServerMessage(clientUsername + " has left the chat.");
    }

    /**
     * Removes clientHandler from clientHandlers and closes all server-side resources associated with this client.
     */
    public void closeAllResources() {
        removeClientHandler();

        Closeable[] resources = new Closeable[]{messageReader, messageWriter, socket};

        try {
            for (Closeable r : resources) {
                if (r != null) {
                    r.close();
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error closing resources: ");
            ioe.printStackTrace();
        }
    }
}
