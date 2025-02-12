package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockModule extends SearchModule {
    private final Set<String> blockTypesToSearchFor = new HashSet<>();

    public BlockModule() {
        super("block");
    }

    @Override
    public void options(String[] options) {
        if (options == null || options.length < 1) {
            throw new IllegalArgumentException("Please specify blocks to search for. ('add block minecraft:bedrock minecraft:water' for example)");
        }
        for (String option : options) {
            if (!option.startsWith("minecraft:")) continue;
            blockTypesToSearchFor.add(option);
        }
    }

    @Override
    public Object processChunk(Chunk chunk) {
        List<String> foundTypes = new ArrayList<>();
        var list = chunk.getData().getList("sections");
        list.stream()
                .map(CompoundTag.class::cast)
                .forEach(sect -> {
                    if (foundTypes.size() == blockTypesToSearchFor.size()) return;
                    var palette = sect.getCompound("block_states").getList("palette");
                    palette.stream()
                            .map(CompoundTag.class::cast)
                            .map(cmpnd -> cmpnd.getString("Name"))
                            .filter(blockTypesToSearchFor::contains)
                            .forEach(foundTypes::add);
        });

        if (foundTypes.isEmpty()) return null;

        List<String> returns = new ArrayList<>();

        for (String found : foundTypes) {
            returns.add(String.format("%d,%d,%s", chunk.getX(), chunk.getZ(), found));
        }
        return returns;
    }




}
