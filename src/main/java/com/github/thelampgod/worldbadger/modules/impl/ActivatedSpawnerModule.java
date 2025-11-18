package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;

import java.util.List;

/**
 * Uses the Delay tag of spawners to determine if a player has been in range of the spawner
 */
public class ActivatedSpawnerModule extends BlockEntitySearchModule {

    public ActivatedSpawnerModule() {
        super("activated-spawner");
    }

    @Override
    public List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities) {
        return blockEntities.stream()
                .filter(tag -> tag.getString("id").equals("minecraft:mob_spawner"))
                .filter(spawner -> {
                    short delay = spawner.getShort("Delay");
                    return delay != 20;
                })
                .map(spawner -> {
                    int x = spawner.getInt("x");
                    int y = spawner.getInt("y");
                    int z = spawner.getInt("z");
                    return new SpawnerData(x, y, z);
                }).toList();
    }

    @Data
    private static class SpawnerData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z);
        }
    }

    @Override
    public String getDescription() {
        return "Finds spawners that have been visited by a player.";
    }
}
