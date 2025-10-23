package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;
import com.github.thelampgod.worldbadger.util.CsvFormatHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CsvOutput implements OutputMode {

    private final CsvFormatHelper csvFormatHelper = new CsvFormatHelper();
    private Path outputFolder;
    private final Map<String, PrintWriter> moduleToWriterMap = new HashMap<>();
    private final Set<String> headersWritten = new HashSet<>(); // Track which modules had headers written

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

        PrintWriter writer = moduleToWriterMap.computeIfAbsent(moduleName, name -> {
            try {
                Path outputFile = outputFolder.resolve(name + ".csv");
                return new PrintWriter(new BufferedWriter(new FileWriter(outputFile.toFile(), true)), true);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create writer for " + moduleName, e);
            }
        });

        // Check if we need to write headers for this module
        boolean writeHeaders = !headersWritten.contains(moduleName);

        for (Object result : results) {
            if (result instanceof DataClass data) {
                if (writeHeaders) {
                    writer.println(String.join(",", data.getFieldNames()));
                    headersWritten.add(moduleName);
                    writeHeaders = false;
                }

                String row = data.getFieldValues().stream()
                        .map(csvFormatHelper::formatCsvValue)
                        .collect(Collectors.joining(","));
                writer.println(row);
            }
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
