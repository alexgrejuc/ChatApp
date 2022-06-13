package com.alexgrejuc.chatclient;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * A client that sends and receives text messages to and from a chat server.
 */
public class Client {
    private Socket socket;
    private BufferedReader messageReader;
    private BufferedWriter messageWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.messageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.messageWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ioe) {
            System.err.println("Error creating client:");
            ioe.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Sends a message from this client to all other clients.
     * @param message
     */
    public void sendMessage(String message) {
        try {
            messageWriter.write(message);
            messageWriter.newLine();
            messageWriter.flush();

            if (message.equalsIgnoreCase(":quit")) {
                closeAllResources();
            }
        } catch (IOException ioe) {
            System.err.println("Cannot send message because the server is offline.");
            closeAllResources();
        }
    }

    /**
     * Logs in to the chat room by sending the client's username.
     */
    public void logIn() {
        try {
            messageWriter.write(username);
            messageWriter.newLine();
            messageWriter.flush();
        } catch (IOException ioe) {
            System.err.println("Error logging in:");
            ioe.printStackTrace();
            closeAllResources();
        }
    }

    /**
     * Listens for messages from other clients and displays them to this client.
     * Since this is a blocking operation, it executes in its own thread.
     */
    public void listenForMessages(VBox vbox_messages) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean serverOnline = true;

                while (!socket.isClosed() && serverOnline) {
                    try {
                        String message = messageReader.readLine();

                        serverOnline = message != null;

                        if (serverOnline) {
                            ClientController.attachSentMessage(message, vbox_messages);
                        }
                        else {
                            System.out.println("Cannot receive messages because the server is offline.");
                        }

                    } catch (SocketException se) {
                        // The socket has been closed from the message sending thread.
                        // The next loop will see the socket is closed and this thread will stop.
                        System.out.println("Goodbye, " + username);
                    }
                    catch (IOException ioe) {
                        System.err.println("Error listening for messages:");
                        ioe.printStackTrace();
                        closeAllResources();
                    }
                }
            }
        }).start();
    }

    public void closeAllResources() {
        Closeable[] resources = new Closeable[]{messageReader, messageWriter, socket};

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
