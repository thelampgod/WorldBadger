package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OldChunksModule extends SearchModule {
    private final Set<String> shitStones = Set.of("minecraft:andesite", "minecraft:diorite", "minecraft:granite");

    public OldChunksModule() {
        super("oldchunks");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        boolean isNewerThan1_7 = false;
        final int x = chunk.getX();
        final int z = chunk.getZ();

        //check for existence of shitstones
        for (int i = 0; i < 5; ++i) {
            CompoundTag section = chunk.getSection(i);
            if (section == null) continue;

            CompoundTag blockStates = section.getCompound("block_states");
            Set<String> blockIds = getPaletteBlockIds(blockStates.getList("palette"));


            for (String stone : shitStones) {
                if (blockIds.contains(stone)) {
                    isNewerThan1_7 = true;
                    break;
                }
            }
            // no need to check any more section
            if (isNewerThan1_7) break;
        }

        // if option "all", return a ChunkData result with state of chunk boolean
        if (idToOptionsMap.get("all") != null) {
            return List.of(new ChunkData(x, z, isNewerThan1_7));
        }

        // otherwise, only return oldchunks positions
        return (isNewerThan1_7) ? null : List.of(new OldChunkData(x, z));
    }

    private Set<String> getPaletteBlockIds(ListTag palette) {
        Set<String> blockIds = new HashSet<>();
        for (Tag tag : palette) {
            String blockId = ((CompoundTag) tag).getString("Name");
            blockIds.add(blockId);
        }
        return blockIds;
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("all"); //return both oldchunks and newchunks data
    }

    @Override
    public boolean requiresId() {
        return false;
    }

    @Data
    private static class ChunkData implements DataClass {
        private final int x;
        private final int z;
        private final boolean isNewerThan1_7;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "z", "isNewerThan1_7");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, z, isNewerThan1_7);
        }
    }

    @Data
    private static class OldChunkData implements DataClass {
        private final int x;
        private final int z;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "z");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, z);
        }
    }
}
