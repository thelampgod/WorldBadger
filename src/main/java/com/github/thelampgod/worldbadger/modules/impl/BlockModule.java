package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class BlockModule extends SearchModule {
    private final Map<String, Map<String, String>> blockNameToOptionsMap = new HashMap<>();

    private final Set<String> validOptions = Set.of("id", "min", "max", "type");

    public BlockModule() {
        super("block");
    }

    @Override
    public void options(String[] options) {
        if (options == null || options.length < 1) {
            throw new IllegalArgumentException("Please specify blocks to search for. ('add block minecraft:bedrock minecraft:water' for example)");
        }
        for (String option : options) {
            final Map<String, String> optionToValueMap = new HashMap<>();

            // "add block id=minecraft:bedrock,type=bla,min=5,max=6 id=bla..." etc

            var blockOptions = option.split(",");

            String id = null;
            for (String blockOption : blockOptions) {
                String[] properties = blockOption.split("=");
                String optionName = properties[0].toLowerCase();
                String val = properties[1];
                if (!validOptions.contains(optionName)) continue;
                if (optionName.equals("id")) {
                    id = val;
                }
                optionToValueMap.put(optionName, val);
            }

            if (id == null) {
                throw new IllegalArgumentException("id option required!");
            }

            blockNameToOptionsMap.put(id, optionToValueMap);
        }

        System.out.println(blockNameToOptionsMap);
    }

    @Override
    public Object processChunk(Chunk chunk) {
        List<String> foundTypes = new ArrayList<>();
        var list = chunk.getData().getList("sections");
        list.stream()
                .map(CompoundTag.class::cast)
                .forEach(sect -> {
                    if (foundTypes.size() == blockNameToOptionsMap.keySet().size()) return;
                    var palette = sect.getCompound("block_states").getList("palette");
                    palette.stream()
                            .map(CompoundTag.class::cast)
                            .map(cmpnd -> cmpnd.getString("Name"))
                            .filter(blockNameToOptionsMap.keySet()::contains)
                            .forEach(foundTypes::add);
        });

        if (foundTypes.isEmpty()) return null;

        List<String> returns = new ArrayList<>();

        for (String found : foundTypes) {
            returns.add(String.format("%d,%d,%s", chunk.getX(), chunk.getZ(), found));
        }
        return returns;
    }



    private boolean contains(String s, String[] arr) {
        for (String i : arr) {
            if (i.equals(s)) return true;
        }
        return false;
    }

}
