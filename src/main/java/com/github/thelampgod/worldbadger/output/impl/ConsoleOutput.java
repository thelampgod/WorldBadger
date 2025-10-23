package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ConsoleOutput implements OutputMode {

    public Gson gson = new Gson();
    @Override
    public void initialize(Path outputFolder) {
        // no-op
    }

    @Override
    public void processChunkResult(String moduleName, List<?> results) throws IOException, RuntimeException {
        for (Object result : results) {
            if (result instanceof DataClass data) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < data.getFieldNames().size(); ++i) {
                    String fieldName = data.getFieldNames().get(i);
                    Object value = data.getFieldValues().get(i);
                    if (!value.getClass().isPrimitive()) {
                        value = gson.toJson(value);
                    }
                    builder.append(fieldName).append("=").append(value).append(",");
                }

                System.out.println(builder.substring(0, builder.length() - 1));
            }
        }
    }

    @Override
    public void close() {
        // no-op
    }
}
