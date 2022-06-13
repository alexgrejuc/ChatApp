package com.alexgrejuc.chatserver.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * The driver code for running the chat server.
 */
public class Main {
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
