package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.thelampgod.worldbadger.util.Helper.SNBT_WRITER;

public class BlockEntityModule extends BlockEntitySearchModule {
    public BlockEntityModule() {
        super("block-entity");
    }

    @Override
    public List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities) {
        List<BlockEntityData> foundBlockEntities = new ArrayList<>();
        boolean all = idToOptionsMap.isEmpty() || idToOptionsMap.containsKey("all");

        for (CompoundTag tag : blockEntities) {
            String id = tag.getString("id");
            if (!all) {
                if (!idToOptionsMap.containsKey(id)) continue;
            }

            Map<String, String> options = idToOptionsMap.get(all ? "all" : id);
            int minY = options != null && options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
            int maxY = options != null && options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;
            boolean printNbt = options != null && options.containsKey("nbt");

            int x = tag.getInt("x");
            int y = tag.getInt("y");
            int z = tag.getInt("z");

            if (y >= minY && y <= maxY) {
                if (printNbt) {
                    foundBlockEntities.add(new BlockEntityData(x, y, z, id, SNBT_WRITER.toString(tag)));
                } else {
                    foundBlockEntities.add(new BlockEntityData(x, y, z, id, null));
                }
            }
        }

        return foundBlockEntities;
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max", "nbt");
    }

    @Override
    public String getDescription() {
        return "Find all block entities matching, optionally with their nbt data. Usage: id=<id>,option=<option>.";
    }

    @Data
    private static class BlockEntityData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        private final String id;

        private final String nbt;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "id", "nbt");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, id, nbt);
        }
    }
}
