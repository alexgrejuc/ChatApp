package com.alexgrejuc.chatserver.server;

import com.alexgrejuc.chatmessage.ChatMessage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Handles an individual client's connection, disconnection, and messaging.
 */
public class ClientHandler implements Runnable {
    public static HashMap<String, ClientHandler> clientHandlers = new HashMap();

    private Socket socket;
    private ObjectInputStream messageInput;
    private ObjectOutputStream messageOutput;
    private String clientUsername;

    /**
     * Creates a client handler, gets the client's senderName, and broadcasts that they have entered the chat.
     * @param socket
     */
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.messageInput = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            this.messageOutput = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            messageOutput.flush();

            ChatMessage loginMessage = (ChatMessage) messageInput.readObject();
            // A client sends their senderName when first logging in
            this.clientUsername = loginMessage.senderName();
            System.out.println(clientUsername + " has connected.");

            clientHandlers.put(this.clientUsername, this);
            broadcastServerMessage(clientUsername + " has entered the chat.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error creating client handler:");
            e.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Listens for a message from a client and broadcasts it to the other clients.
     */
    @Override
    public void run() {
        ChatMessage messageFromClient;

        // isClosed checks that socket has not been closed from the server side (e.g. due to exception)
        while (!socket.isClosed()) {
            try {
                messageFromClient = (ChatMessage) messageInput.readObject();

                if (messageFromClient != null && !messageFromClient.message().equalsIgnoreCase(":quit")) {
                    sendMessage(populateRecipients(messageFromClient));
                }
                else {
                    closeAllResources();
                }
            }
             catch (IOException | ClassNotFoundException e) {
                System.err.println("Error reading message from client: ");
                e.printStackTrace();
                closeAllResources();
            }
        }
    }

    /**
     * Inserts all recipients except for the sending client in the case that this is a broadcast message.
     * @param messageFromClient
     * @return
     */
    private ChatMessage populateRecipients(ChatMessage messageFromClient) {
        // The client has specified recipients, it is not a broadcast message
        if (messageFromClient.recipientNames() != null) {
            return messageFromClient;
        }
        else {
            var recipients = clientHandlers.keySet().stream()
                                                                   .filter(name -> !name.equals(clientUsername))
                                                                   .collect(Collectors.toCollection(ArrayList::new));

            return new ChatMessage(messageFromClient.message(), messageFromClient.senderName(), recipients, messageFromClient.attachments());
        }
    }

    /**
     * Sends a message to a single recipient.
     * @param message
     * @param recipientName
     */
    private void sendMessageToOne(ChatMessage message, String recipientName) {
        if (clientHandlers.containsKey(recipientName)) {
            try {
                var recipient = clientHandlers.get(recipientName);
                recipient.messageOutput.writeObject(message);
                recipient.messageOutput.flush();
            } catch (IOException ioe) {
                System.err.println("Error sending message from " + this.clientUsername + " to " + recipientName + ":");
                ioe.printStackTrace();
                closeAllResources();
            }
        }
    }

    /**
     * Sends a message to all the recipientNames specified by the client.
     * @param message
     */
    private void sendMessage(ChatMessage message) {
        for (String recipient : message.recipientNames()) {
            sendMessageToOne(message, recipient);
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
                sendMessageToOne(new ChatMessage(message, "SERVER", new ArrayList<>(), new ArrayList<>()), clientHandler.clientUsername);
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

        Closeable[] resources = new Closeable[]{messageInput, messageOutput, socket};

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
