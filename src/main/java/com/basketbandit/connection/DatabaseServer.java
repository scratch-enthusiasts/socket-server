package com.basketbandit.connection;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseServer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseServer.class);
    private static DBConfigurationBuilder dbConfig;
    private static DB database;
    private static HikariConfig config;
    private static HikariDataSource connectionPool;

    public DatabaseServer(int port) throws ManagedProcessException {
        dbConfig = DBConfigurationBuilder.newBuilder();
        dbConfig.setPort(port);
        dbConfig.addArg("--character-set-server=utf8mb4");
        dbConfig.addArg("--collation-server=utf8mb4_general_ci"); // current version doesn't recognise utf8mb4_0900_ai_ci

        config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost/dbmusicparty?useSSL=true&serverTimezone=UTC");
        config.setUsername("root");
        config.addDataSourceProperty("cachePrepStmts" , "true");
        config.addDataSourceProperty("prepStmtCacheSize" , "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
        config.setConnectionInitSql("SET NAMES utf8mb4");
    }

    public void start() throws ManagedProcessException {
        database = DB.newEmbeddedDB(dbConfig.build());
        database.start();
        database.createDB("dbmusicparty");
        connectionPool = new HikariDataSource(config);

        // run sql script that sets up database
        try(InputStream sqlStream = DatabaseServer.class.getClassLoader().getResourceAsStream("db.sql");
            BufferedReader reader = new BufferedReader(new InputStreamReader(sqlStream));
            Connection connection = getConnection()) {

            StringBuilder string = new StringBuilder();
            reader.lines().forEach(string::append);
            String[] queries = string.toString().split(";");
            for(String query : queries) {
                connection.prepareStatement(query).execute();
            }

        } catch(Exception e) {
            log.error("There was a problem running the startup sql database scripts, message: {}", e.getMessage());
        }
    }

    /**
     * Stops connection pool and database server, enabling them to be removed successfully.
     */
    public void shutdown() {
        try {
            connectionPool.close();
            database.stop();
        } catch(ManagedProcessException e) {
            log.error("Error while trying to stop database, message: {}", e.getMessage(), e);
        }
    }

    /**
     * Get a fresh database connection.
     * @return {@link Connection}
     */
    public static Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
}
