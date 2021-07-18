package com.basketbandit;

import ch.vorburger.exec.ManagedProcessException;
import com.basketbandit.connection.DatabaseServer;
import com.basketbandit.connection.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

public class MusicParty {
    private static final Logger log = LoggerFactory.getLogger(MusicParty.class);
    private DatabaseServer databaseServer;
    private static boolean databaseEnabled = false;
    private SocketServer socketServer;

    public static void main(String[] args) {
        databaseEnabled = args.length > 0;
        new MusicParty();
    }

    public MusicParty() {
        try {
            if(databaseEnabled) {
                this.databaseServer = new DatabaseServer(3306);
                this.databaseServer.start();
            }
        } catch(ManagedProcessException e) {
            log.error("There was an error starting the database, message: {}", e.getMessage(), e);
        }

        try {
            this.socketServer = new SocketServer(3333);
            this.socketServer.setName("Socket Server Thread");
            this.socketServer.start();

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine()) {
                if(sc.nextLine().equals("exit")) {
                    shutdown();
                }
            }

        } catch(IOException e) {
            log.error("There was an error loading the configuration file, message: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        if(databaseServer != null) {
            databaseServer.shutdown();
        }
        if(socketServer != null) {
            socketServer.shutdown();
        }
        System.exit(0);
    }
}
