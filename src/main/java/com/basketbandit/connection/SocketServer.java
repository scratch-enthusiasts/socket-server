package com.basketbandit.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class SocketServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private final List<SocketClient> clients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket socket;
    private final int port;
    private String passcode;
    private boolean active = true;

    public SocketServer(int port) throws IOException {
        this.port = port;
        this.passcode = generatePasscode();
    }

    public void run() {
        try {
            log.info("Starting socket server on port " + port);
            this.socket = new ServerSocket(port);
            while(active) {
                new SocketClient(socket.accept()).start();
            }
            socket.close();
        } catch(IOException e) {
            log.error("There was a problem with the socket server, message: {}", e.getMessage());
        }
    }

    public void shutdown() {
        active = false;
    }

    /**
     * Generates a 5 digit passcode
     * @return {@link String}
     */
    public String generatePasscode() {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final Random r = new Random(System.currentTimeMillis());
        StringBuilder passcode = new StringBuilder(5);
        for(int i = 0; i < 5; i++) {
            passcode.append(chars.charAt(r.nextInt(chars.length())));
        }
        return passcode.toString();
    }

    /**
     * Broadcasts a message to each client - this would most commonly be something like a track update
     * @param message {@link String}
     */
    public void broadcast(String message) {
        clients.stream()
                .filter(client -> client.in != null && client.out != null)
                .forEach(client -> client.out.println(message));
    }

    private class SocketClient extends Thread {
        private final Socket client;
        private final String clientAddress;
        private String clientNickname;
        private boolean greeted = false;
        private BufferedReader in;
        private PrintWriter out;

        public SocketClient(Socket socket) {
            this.client = socket;
            this.clientAddress = client.getInetAddress().getHostAddress();
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                log.info("Client connected from address '" + clientAddress + "'");

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    // client socket passcode checking
                    if(!greeted) {
                        if(inputLine.equals(passcode)) {
                            greeted = true;
                            out.println(inputLine);
                            continue;
                        }
                        clients.remove(clients.stream().filter(client -> client.clientNickname.equals(clientNickname)).findFirst().get());
                        log.warn("Client from address '{}' was disconnected - incorrect passcode.", clientAddress);
                        out.println("INCORRECT_PASSCODE");
                        break;
                    }

                    // Client socket identification signal
                    if(inputLine.startsWith("@")) {
                        final String input = inputLine.substring(1, Math.min(inputLine.length(), 32));
                        if(clients.stream().noneMatch(socketClient -> socketClient.clientNickname.equals(input))) {
                            log.info("Client from address '{}' identified as '{}'", clientAddress, (clientNickname = input));
                            clients.add(this);
                            out.println(inputLine);
                            continue;
                        }
                        log.info("Client from address '{}' rejected due to duplicate nickname.", clientAddress);
                        out.println("NICKNAME_IN_USE");
                        break;
                    }

                    // Client socket shutdown signal
                    if(inputLine.equals(".")) {
                        clients.remove(clients.stream().filter(client -> client.clientAddress.equals(clientAddress)).findFirst().get());
                        log.info(clientNickname + " disconnected!");
                        out.println("GOODBYE");
                        break;
                    }
                }

                in.close();
                out.close();
                client.close();
            } catch(Exception e) {
                log.warn("There was a problem with client from address '{}', message: {}", clientAddress, e.getMessage());
                if(e.getMessage().equals("Connection reset")) {
                    if(clients.remove(this)) {
                        log.info("Client from address '{}' was successfully disconnected", clientAddress);
                        return;
                    }
                    log.error("Unable to disconnect client from address '{}', nickname '{}' locked", clientAddress, clientNickname);
                }
            }
        }

        /**
         * processes any non-meta-io related commands
         */
        private boolean executeInput(String input) {
            try {
            } catch(Exception e) {
                log.error("There was a problem executing that command, message: {}", e.getMessage(), e);
                return false;
            }
            return false;
        }
    }
}
