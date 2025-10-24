package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Getter;
import lombok.Setter;
import net.querz.mca.Chunk;

import java.util.*;

@Getter
@Setter
public abstract class SearchModule {

    private final String name;
    private boolean toggled = false;

    protected final Map<String, Map<String, String>> idToOptionsMap = new HashMap<>();

    public SearchModule(String name) {
        this.name = name;
    }

    public List<? extends DataClass> processChunk(Chunk chunk) {
        return List.of();
    }


    public void toggle() {
        this.toggled = !toggled;
    }

    public void options(String[] args) {
        if (getValidOptions() == null || getValidOptions().isEmpty()) {
            return;
        }

        if (requiresId() && (args == null || args.length == 0)) {
            throw new NoSuchElementException("Missing 'id' argument.");
        }

        for (String arg : args) {
            String[] options = arg.split(",");
            Map<String, String> optionsMap = getOptionsMap(options);

            String id = optionsMap.get("id");
            if (id == null) {
                if (requiresId()) {
                    throw new NoSuchElementException("Missing 'id' argument.");
                }

                id = "all";
            }
            idToOptionsMap.put(id, optionsMap);
        }
    }

    private Map<String, String> getOptionsMap(String[] options) {
        Map<String, String> optionsMap = new HashMap<>();
        for (String option : options) {
            String[] pair = option.split("=");
            String key = pair[0];
            String val = (pair.length > 1 ? pair[1] : "true");

            if (!getValidOptions().contains(key)) {
                throw new IllegalArgumentException(
                        key + " is not a valid argument. Choose between: " + getValidOptions()
                );
            }
            optionsMap.put(key, val);
        }
        return optionsMap;
    }

    public List<String> getValidOptions() {
        return null;
    }

    public boolean requiresId() {
        return true;
    }
}
