package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.List;

public class ConsoleOutput implements OutputMode {
    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    @Override
    public void initialize(Path outputFolder) {
        // no-op
    }

    @Override
    public void processChunkResult(String moduleName, List<? extends DataClass> results) {
        if (results == null || results.isEmpty()) return;

        for (DataClass data : results) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < data.getFieldNames().size(); ++i) {
                String fieldName = data.getFieldNames().get(i);
                Object value = data.getFieldValues().get(i);
                if (value == null) {
                    value = "";
                }
                if (!value.getClass().isPrimitive()) {
                    value = gson.toJson(value);
                }
                builder.append(fieldName).append("=").append(value).append(",");
            }

            System.out.println(builder.substring(0, builder.length() - 1));
        }
    }

    @Override
    public void close() {
        // no-op
    }
}
