package com.github.thelampgod.worldbadger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvFormatHelper {
    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public String formatCsvValue(Object value) {
        if (value == null) return "";

        if (value instanceof Object[] array) {
            return formatArrayForCsv(array);
        }
        if (value instanceof List<?> list) {
            return formatArrayForCsv(list.toArray());
        }

        String stringValue;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            stringValue = String.valueOf(value);
        } else {
            stringValue = gson.toJson(value);
        }

        return escapeCsvField(stringValue);
    }

    private String formatArrayForCsv(Object[] array) {
        return Arrays.stream(array)
                .map(element -> {
                    if (element == null) return "";
                    String str;
                    if (element instanceof String || element instanceof Number || element instanceof Boolean) {
                        str = String.valueOf(element);
                    } else {
                        str = gson.toJson(element);
                    }
                    return escapeCsvField(str);
                })
                .collect(Collectors.joining(";"));
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
