package com.basketbandit.connection;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SocketServerTest {

    @Test
    void socketCycle() throws IOException {
        SocketServer server = new SocketServer(9999);
        server.shutdown();
    }

    @Test
    void databaseCycle() throws ManagedProcessException {
        DatabaseServer database = new DatabaseServer(3306);
        database.start();
        database.shutdown();
    }

    @Test
    void generatePasscode() throws IOException {
        SocketServer server = new SocketServer(9999);

        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        assert server.generatePasscode().length() == 5;
        for(String character : server.generatePasscode().split("")) {
            assert chars.contains(character);
        }

        server.shutdown();
    }
}