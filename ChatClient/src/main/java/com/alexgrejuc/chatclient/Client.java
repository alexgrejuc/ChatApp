package com.alexgrejuc.chatclient;

import com.alexgrejuc.chatmessage.ChatMessage;
import com.alexgrejuc.chatmessage.ChatMessageParser;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * A client that sends and receives text messages to and from a chat server.
 */
public class Client {
    private Socket socket;
    private ObjectInputStream messageInput;
    private ObjectOutputStream messageOutput;

    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            // Note: this order matters due blocking on the stream header
            // See constructor docs for more info
            this.messageOutput = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            messageOutput.flush();
            this.messageInput = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException ioe) {
            System.err.println("Error creating client:");
            ioe.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Sends a message from this client to the intended recipients.
     * @param message
     */
    public void sendMessage(ChatMessage message) {
        try {
            messageOutput.writeObject(message);
            messageOutput.flush();
        } catch (IOException ioe) {
            System.err.println("Cannot send message because the server is offline:");
            ioe.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Parses user input and sends it as a message from this client to the intended recipients.
     * @param input
     */
    public void sendMessageFromInput(String input, ArrayList<File> attachments) {
        ChatMessage message = ChatMessageParser.parse(input, username, attachments);
        sendMessage(message);
    }

    /**
     * Logs in to the chat room by sending the client's senderName.
     */
    public void logIn() {
        sendMessage(new ChatMessage(null, username, null, new ArrayList<>()));
    }

    /**
     * Logs out of the chat room and closes this client's resources.
     */
    public void logOut() {
        sendMessageFromInput(":quit", new ArrayList<>());
        closeAllResources();
    }

    /**
     * Listens for messages from other clients and displays them to this client.
     * Since this is a blocking operation, it executes in its own thread.
     */
    public void listenForMessages(VBox vbox_messages) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!socket.isClosed()) {
                    try {
                        ChatMessage message = (ChatMessage) messageInput.readObject();
                        ClientController.attachReceivedMessage(message.senderName() + ": " + message.message(), vbox_messages);

                        // TODO: download the images
                        for (var a : message.attachments()) {
                            ClientController.attachReceivedMessage(a.getName(), vbox_messages);
                        }
                    } catch (EOFException eof) {
                        System.out.println("Cannot receive messages because the server is offline.");
                        closeAllResources();
                    }
                    catch (SocketException se) {
                        // The socket has been closed from the message sending thread.
                        // The next loop will see the socket is closed and this thread will stop.
                        System.out.println("Goodbye, " + username);
                    }
                    catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error listening for messages:");
                        e.printStackTrace();
                        closeAllResources();
                    }
                }
            }
        }).start();
    }

    public void closeAllResources() {
        Closeable[] resources = new Closeable[]{messageInput, messageOutput, socket};

        try {
            socket.close();
            for (Closeable r : resources) {
                if (r != null) {
                    r.close();
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error closing resources:");
            ioe.printStackTrace();
        }
    }
}
