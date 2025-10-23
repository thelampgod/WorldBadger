package com.github.thelampgod.worldbadger.database;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.google.gson.Gson;
import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Database {

    public Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final Connection connection;

    private final Gson gson = new Gson();

    public Database(String url) throws SQLException {
        this.connection = DriverManager.getConnection(url);
        // Configure SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA synchronous = NORMAL");
            stmt.execute("PRAGMA cache_size = -64000");
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void createTable(String moduleName, DataClass data) {
        String query = """
                CREATE TABLE IF NOT EXISTS "{moduleName}" (
                {columns}
                );
                """;

        String columns = createColumnsQuery(data);
        query = query
                .replace("{moduleName}", moduleName)
                .replace("{columns}", columns);


        try (Statement stmt = this.getConnection().createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createColumnsQuery(DataClass data) {
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < data.getFieldNames().size(); ++i) {
            String column = """
                    "{fieldName}" {fieldValueType} NOT NULL
                    """;
            String fieldName = data.getFieldNames().get(i);
            Object fieldValue = data.getFieldValues().get(i);

            String fieldValueType = getSqlType(fieldValue);

            query.append(column
                            .replace("{fieldName}", fieldName)
                            .replace("{fieldValueType}", fieldValueType))
                    .append(",");
        }
        return query.substring(0, query.length() - 1);
    }

    private String getSqlType(Object value) {
        if (value == null) {
            return "TEXT"; // Default fallback
        }

        Class<?> type = value.getClass();

        if (type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Short.class || type == short.class ||
                type == Byte.class || type == byte.class) {
            return "INTEGER";
        }

        if (type == Double.class || type == double.class ||
                type == Float.class || type == float.class) {
            return "REAL";
        }

        if (type == Boolean.class || type == boolean.class) {
            return "BOOLEAN";
        }

        // Handle arrays and collections as JSON
        if (type.isArray() || value instanceof Collection) {
            return "TEXT"; // Store as JSON string
        }

        // Everything else (String, custom objects) as TEXT
        return "TEXT";
    }

    public void insertBatch(String moduleName, List<? extends DataClass> dataList) {
        if (dataList.isEmpty()) return;

        List<String> fieldNames = dataList.get(0).getFieldNames();

        String placeholders = fieldNames.stream()
                .map(f -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format(
                "INSERT INTO \"%s\" (%s) VALUES (%s)",
                moduleName,
                String.join(", ", fieldNames.stream()
                        .map(name -> "\"" + name + "\"")
                        .collect(Collectors.toList())),
                placeholders
        );

        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            for (DataClass data : dataList) {
                List<Object> fieldValues = data.getFieldValues();

                for (int i = 0; i < fieldValues.size(); i++) {
                    setPreparedStatementValue(stmt, i + 1, fieldValues.get(i));
                }

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert into " + moduleName, e);
        }
    }

    private void setPreparedStatementValue(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            stmt.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value.getClass().isArray() || value instanceof Collection) {
            // Convert arrays/collections to JSON
            stmt.setString(index, gson.toJson(value));
        } else {
            // Convert any other object to JSON
            stmt.setString(index, gson.toJson(value));
        }
    }
}
