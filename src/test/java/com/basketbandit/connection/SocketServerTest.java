package com.basketbandit.connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SocketServerTest {
    private SocketServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new SocketServer(9999);
    }

    @AfterEach
    void tearDown() {
        server.shutdown();
    }

    @Test
    void generatePasscode() {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        assert server.generatePasscode().length() == 5;
        for(String character : server.generatePasscode().split("")) {
            assert chars.contains(character);
        }
    }
}