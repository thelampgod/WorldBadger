package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class BlockModule extends SearchModule {

    public BlockModule() {
        super("block");
    }

    @Override
    public Object processChunk(Chunk chunk) {
        List<String> foundTypes = new ArrayList<>();
        var list = chunk.getData().getList("sections");
        list.stream()
                .map(CompoundTag.class::cast)
                .forEach(sect -> {
                    if (foundTypes.size() == idToOptionsMap.keySet().size()) return;
                    var palette = sect.getCompound("block_states").getList("palette");
                    palette.stream()
                            .map(CompoundTag.class::cast)
                            .map(cmpnd -> cmpnd.getString("Name"))
                            .filter(idToOptionsMap.keySet()::contains)
                            .forEach(foundTypes::add);
        });

        if (foundTypes.isEmpty()) return null;

        List<String> returns = new ArrayList<>();

        for (String found : foundTypes) {
            returns.add(String.format("%d,%d,%s", chunk.getX(), chunk.getZ(), found));
        }
        return returns;
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max", "type");
    }
}
