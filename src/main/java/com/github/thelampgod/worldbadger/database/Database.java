package com.github.thelampgod.worldbadger.database;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.util.Helper;
import org.apache.commons.dbcp2.BasicDataSource;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final WorldBadger instance;
    private final BasicDataSource database;


    public Database(WorldBadger instance, String url) {
        this.instance = instance;
        this.database = connect(url);
    }

    private BasicDataSource connect(String url) {
        final BasicDataSource db = new BasicDataSource();
        db.setDriverClassName("org.sqlite.JDBC");
        db.setUrl(url);
        db.setInitialSize(1);

        instance.logger.info("Connected to database.");
        return db;
    }

    public Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    public void applySchema() {
        //create tables from schema in resources/schema.sql
        URL url = Thread.currentThread().getContextClassLoader().getResource("schema.sql");
        if (url == null) return;
        try {
            Path schema = Helper.getResourceAsPath(url, "schema.sql");
            String[] queries = Files.readString(schema).split(";");

            try (Statement stmt = this.database.getConnection().createStatement()) {
                for (String query : queries) {
                    stmt.addBatch(query);
                }
                stmt.executeBatch();
            }

        } catch (Exception e) {
            System.err.println("Error: couldn't read schema: " + e.getMessage());
        }
    }
}
