package com.alexgrejuc.chatapp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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
     * Gets a message from this client.
     * @param scanner
     * @return the message that a user would like to send.
     */
    public String getMessage(Scanner scanner) {
        return scanner.nextLine();
    }

    /**
     * Sends a message from this client to all other clients.
     * @param message
     * @throws IOException
     */
    private void sendMessage(String message) throws IOException {
        messageWriter.write(message);
        messageWriter.newLine();
        messageWriter.flush();
    }

    /**
     * Sends messages from this client to all other clients.
     */
    public void sendMessages() {
        boolean serverOnline = true;
        try {
            Scanner scanner = new Scanner(System.in);

            while (!socket.isClosed() && serverOnline) {
                String message = getMessage(scanner);
                sendMessage(message);
            }
        } catch (IOException ioe) {
            serverOnline = false;
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
    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean serverOnline = true;

                while (!socket.isClosed() && serverOnline) {
                    try {
                        String message = messageReader.readLine();

                        serverOnline = message != null;

                        if (serverOnline) {
                            System.out.println(message);
                        }
                        else {
                            System.out.println("Cannot receive messages because the server is offline.");
                        }

                    } catch (IOException ioe) {
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

    /**
     * Gets a username from the client and connects to the server.
     * Loops indefinitely in one thread for sending messages and another for receiving them.
     * @param args
     */
    public static void main(String[] args) throws IOException {
        try {
            Socket socket = new Socket("localhost", 7777);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username for the group chat: ");
            String username = scanner.nextLine();

            Client client = new Client(socket, username);
            client.logIn();

            // Infinite loop to read and send messages.
            client.listenForMessages();
            client.sendMessages();
        } catch (IOException ioe) {
            System.out.println("Error connecting to the server. Perhaps it is offline or your configuration is incorrect.");
        }

        System.out.println("Terminating execution.");
    }
}
