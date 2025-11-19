package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public class SpawnerModule extends BlockEntitySearchModule {

    public SpawnerModule() {
        super("spawner");
    }

    @Override
    public List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities) {
        boolean all = idToOptionsMap.isEmpty() || idToOptionsMap.containsKey("all");

        return blockEntities.stream()
                .filter(tag -> tag.getString("id").equals("minecraft:mob_spawner"))
                .filter(spawner -> {
                    String entityId = spawner.getCompound("SpawnData").getCompound("entity").getString("id");
                    if (!all) {
                        if (!idToOptionsMap.containsKey(entityId)) return false;
                    }
                    Map<String, String> options = idToOptionsMap.get(all ? "all" : entityId);
                    int minY = options != null && options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
                    int maxY = options != null && options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;

                    int y = spawner.getInt("y");

                    return y >= minY && y <= maxY;
                })
                .map(spawner -> {
                    int x = spawner.getInt("x");
                    int y = spawner.getInt("y");
                    int z = spawner.getInt("z");
                    String entityId = spawner.getCompound("SpawnData").getCompound("entity").getString("id");

                    return new SpawnerData(x,y,z,entityId);
                }).toList();
    }

    @Data
    private static class SpawnerData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        private final String entityId;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "entityId");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x,y,z,entityId);
        }
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max");
    }

    @Override
    public boolean requiresId() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Finds spawner block entities, optionally by entity-type.";
    }
}
