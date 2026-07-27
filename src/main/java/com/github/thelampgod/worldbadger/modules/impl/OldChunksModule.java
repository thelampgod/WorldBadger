package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.util.blocks.BlockState;
import com.github.thelampgod.worldbadger.util.blocks.BlockUtils;
import lombok.Data;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.StringTag;
import net.querz.nbt.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OldChunksModule extends SearchModule {
    private final Set<String> stoneTypes = Set.of("minecraft:andesite", "minecraft:diorite", "minecraft:granite");
    private final WorldBadger instance;
    private Integer dimensionId = null;

    public OldChunksModule(WorldBadger instance) {
        super("oldchunks");
        this.instance = instance;
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        if (this.dimensionId == null) {
            this.dimensionId = instance.getWorldManager().getWorld().getDimension();
        }

        final int x = chunk.getX();
        final int z = chunk.getZ();

        ChunkGenVersion version = switch (dimensionId) {
            case 0 -> analyzeOverworld(chunk);
            case -1 -> analyzeNether(chunk);
            default -> null;
        };

        if (dimensionId == 0 && idToOptionsMap.get("onlyOld") != null) {
            return (version != ChunkGenVersion.PRE_1_8) ? null : List.of(new OldChunkData(x, z));
        }

        return List.of(new ChunkData(x, z, version));
    }

    private ChunkGenVersion analyzeOverworld(Chunk chunk) {
        if (containsDeepslate(chunk)) {
            return ChunkGenVersion.V1_18;
        } else if (containsStoneTypes(chunk)) {
            return ChunkGenVersion.V1_8;
        } else return ChunkGenVersion.PRE_1_8;
    }

    private boolean containsDeepslate(Chunk chunk) {
        CompoundTag section = chunk.getSection(0);
        if (section == null) return false;

        // all worlds (that have been converted) should have deepslate in section 0, but only 1.18+ have deepslate above y5
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockState block = BlockUtils.getBlockAtCoordinate(chunk, (chunk.getX() << 4) + x, 6, (chunk.getZ() << 4) + z);
                if (block == null) continue;
                if (block.getId().equals("minecraft:deepslate")) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsStoneTypes(Chunk chunk) {
        for (int i = 0; i < 5; ++i) {
            CompoundTag section = chunk.getSection(i);
            if (section == null) continue;

            CompoundTag blockStates = section.getCompound("block_states");
            Set<String> blockIds = getPaletteBlockIds(blockStates.getList("palette"));

            for (String stone : stoneTypes) {
                if (blockIds.contains(stone)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChunkGenVersion analyzeNether(Chunk chunk) {
        if (containsNewBiome(chunk) || containsNetherGold(chunk)) {
            return ChunkGenVersion.V1_16;
        } else if (containsMagma(chunk)) {
            return ChunkGenVersion.V1_10;
        } else if (containsQuartz(chunk)) {
            return ChunkGenVersion.V1_5;
        } else return ChunkGenVersion.PRE_1_5;
    }

    private boolean containsNetherGold(Chunk chunk) {
        for (CompoundTag section : chunk.getSectionParser()) {
            CompoundTag blockStates = section.getCompound("block_states");
            Set<String> blockIds = getPaletteBlockIds(blockStates.getList("palette"));

            if (blockIds.contains("minecraft:nether_gold_ore")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for magma in sections 1 & 2
     * Magma only generates in between y27-y36
     */
    private boolean containsMagma(Chunk chunk) {
        for (int i = 1; i < 3; ++i) {
            CompoundTag section = chunk.getSection(i);
            if (section == null) continue;

            CompoundTag blockStates = section.getCompound("block_states");
            Set<String> blockIds = getPaletteBlockIds(blockStates.getList("palette"));

            if (blockIds.contains("minecraft:magma_block")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsQuartz(Chunk chunk) {
        for (CompoundTag section : chunk.getSectionParser()) {
            CompoundTag blockStates = section.getCompound("block_states");
            Set<String> blockIds = getPaletteBlockIds(blockStates.getList("palette"));

            if (blockIds.contains("minecraft:nether_quartz_ore")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the chunk has a biome other than nether_wastes
     */
    private boolean containsNewBiome(Chunk chunk) {
        CompoundTag section = chunk.getSection(5); // the y doesnt matter much, use 5 (y64)
        if (section == null) return false;

        CompoundTag biomes = section.getCompound("biomes");
        if (biomes == null) return false;

        ListTag biomesList = biomes.getList("palette");
        for (Tag tag : biomesList) {
            String name = ((StringTag) tag).getValue();
            // also ignore if the biome is plains - edge case for 2b2t/corrupted worlds
            if (!(name.equalsIgnoreCase("minecraft:nether_wastes") || name.equalsIgnoreCase("minecraft:plains"))) {
                return true;
            }
        }

        return false;
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
        return List.of("onlyOld"); //only return oldchunk (pre 1.8) positions (overworld)
    }

    @Override
    public boolean requiresId() {
        return false;
    }

    @Data
    private static class ChunkData implements DataClass {
        private final int x;
        private final int z;
        private final ChunkGenVersion chunkGenVersion;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "z", "chunkGenVersion");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, z, chunkGenVersion);
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

    public enum ChunkGenVersion {
        //overworld
        PRE_1_8,
        V1_8,
        V1_18,
        //nether
        PRE_1_5,
        V1_5,
        V1_10,
        V1_16,
    }

    @Override
    public String getDescription() {
        return "Determine chunk generation version via block and biome analysis. Optionally (for overworld) return only pre 1.8 oldchunk positions.";
    }
}
