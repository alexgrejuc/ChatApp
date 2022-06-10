package com.alexgrejuc.chatapp.server;

import com.alexgrejuc.chatapp.message.MessageInfo;
import com.alexgrejuc.chatapp.message.MessageInfoParser;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * Handles an individual client's connection, disconnection, and messaging.
 */
public class ClientHandler implements Runnable {
    public static HashMap<String, ClientHandler> clientHandlers = new HashMap();

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
            System.out.println(clientUsername + " has connected.");

            clientHandlers.put(this.clientUsername, this);
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
                clientConnected = messageFromClient != null && !messageFromClient.equalsIgnoreCase(":quit");

                if (clientConnected) {
                    MessageInfo mi = MessageInfoParser.parse(messageFromClient);
                    //broadcastMessage(messageFromClient);
                    sendMessage(mi);
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
     * Sends a message to a single recipient.
     * @param message
     * @param recipientName
     */
    private void sendMessageToOne(String message, String sender, String recipientName) {
        if (clientHandlers.containsKey(recipientName)) {
            try {
                var recipient = clientHandlers.get(recipientName);
                recipient.messageWriter.write(sender + ": " + message);
                recipient.messageWriter.newLine();
                recipient.messageWriter.flush();
            } catch (IOException ioe) {
                System.err.println("Error sending message from " + this.clientUsername + " to " + recipientName + ":");
                ioe.printStackTrace();
                closeAllResources();
            }
        }
    }

    /**
     * Sends a message to all the recipients specified by the client.
     * @param message
     */
    private void sendMessage(MessageInfo message) {
        // User specified recipients, so only send to them
        if (message.recipients.isPresent()) {
            for (String recipient : message.recipients.get()) {
                sendMessageToOne(message.message, clientUsername, recipient);
            }
        }
        else {
            // Usernames were not specified in the message string, so message all the other users
            for (String recipient : clientHandlers.keySet()) {
                if (!recipient.equals(clientUsername)) {
                    sendMessageToOne(message.message, clientUsername, recipient);
                }
            }
        }

    }

    /**
     * Broadcasts a message from the server to all clients except this one.
     * e.g. "Alice has entered the chat."
     * @param message
     */
    public void broadcastServerMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            if (clientHandler != this) {
                sendMessageToOne(message, "SERVER", clientHandler.clientUsername);
            }
        }
    }

    /**
     * Removes clientHandler from clientHandlers to ensure no future messages are sent to it.
     */
    public void removeClientHandler() {
        clientHandlers.remove(this.clientUsername);

        String quitMessage = clientUsername + " has left the chat";
        System.out.println(quitMessage);
        broadcastServerMessage(quitMessage);
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
