package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.database.Database;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseOutput implements OutputMode {
    private Database database;

    private final Set<String> tablesCreated = new HashSet<>(); // Track which modules had headers written

    @Override
    public void initialize(Path outputFolder) throws Exception {
        if (outputFolder.toFile().exists()) {
            throw new Exception(outputFolder.toString() + " exists! Delete the folder or choose a different output.");
        }

        Files.createDirectories(outputFolder);

        Path dbFile = outputFolder.resolve("worldbadger.db");
        this.database = new Database("jdbc:sqlite:" + dbFile.toString());
    }

    @Override
    public void processChunkResult(String moduleName, List<? extends DataClass> results) {
        if (results == null || results.isEmpty()) return;

        // Check if we need to create tables for this module
        boolean createTables = !tablesCreated.contains(moduleName);

        if (createTables) {
            DataClass data = results.get(0);
            database.createTable(moduleName, data);
            tablesCreated.add(moduleName);
        }

        database.insertBatch(moduleName, results);
    }

    @Override
    public void close() {
        try {
            database.close();
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
        tablesCreated.clear();
    }
}
