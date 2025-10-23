package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;
import com.google.gson.Gson;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CsvOutput implements OutputMode {

    private final Gson gson = new Gson();
    private Path outputFolder;
    private final Map<String, PrintWriter> moduleToWriterMap = new HashMap<>();

    @Override
    public void initialize(Path outputFolder) throws Exception {
        if (outputFolder.toFile().exists()) {
            throw new Exception(outputFolder.toString() + " exists! Delete the folder or choose a different output.");
        }

        Files.createDirectories(outputFolder);
        this.outputFolder = outputFolder;
    }

    @Override
    public void processChunkResult(String moduleName, List<?> results) {
        if (results == null || results.isEmpty()) return;
        AtomicBoolean shouldWriteHeaders = new AtomicBoolean(false);

        try {
            PrintWriter writer = moduleToWriterMap.computeIfAbsent(moduleName, name -> {
                shouldWriteHeaders.set(true);
                try {
                    Path outputFile = outputFolder.resolve(name + ".csv");
                    return new PrintWriter(new BufferedWriter(new FileWriter(outputFile.toFile(), true)), true);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create writer for " + moduleName, e);
                }
            });

            for (Object result : results) {
                if (result instanceof DataClass data) {
                    if (shouldWriteHeaders.get()) {
                        writer.println(Strings.join(data.getFieldNames(), ','));
                        shouldWriteHeaders.set(false);
                    }

                    String row = data.getFieldValues().stream()
                            .map(d -> d.getClass().isPrimitive() ? d.toString() : gson.toJson(d))
                            .collect(Collectors.joining(","));
                    writer.println(row);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to write CSV for " + moduleName, e);
        }
    }

    @Override
    public void close() {
        moduleToWriterMap.forEach((module, writer) -> {
            writer.close();
        });
        moduleToWriterMap.clear();
    }
}
