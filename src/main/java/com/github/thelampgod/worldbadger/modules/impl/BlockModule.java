package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.*;

public class BlockModule extends SearchModule {

    public BlockModule() {
        super("block");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        List<BlockData> foundBlocks = new ArrayList<>();
        var list = chunk.getData().getList("sections");

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        for (Object obj : list) {
            CompoundTag section = (CompoundTag) obj;
            int ySection = section.getByte("Y") << 4; // Convert section index to absolute Y

            // Process this section
            foundBlocks.addAll(processSection(chunkX, chunkZ, ySection, section));
        }

        return foundBlocks;
    }

    @Data
    private static class BlockData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        private final String blockId;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "blockId");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, blockId);
        }
    }

    private List<BlockData> processSection(int chunkX, int chunkZ, int ySection, CompoundTag section) {
        List<BlockData> foundBlocks = new ArrayList<>();

        if (!section.containsKey("block_states")) {
            // sometimes section 20 exists and has no block_state, lets just ignore this for now
            // TODO: log maybe?
            return foundBlocks;
        }

        CompoundTag blockStates = section.getCompound("block_states");
        ListTag palette = blockStates.getList("palette");

        Set<Integer> relevantPaletteIndices = getRelevantPaletteIndices(palette);
        if (relevantPaletteIndices.isEmpty()) return foundBlocks; // Skip section if no matching blocks

        if (!blockStates.containsKey("data") || blockStates.getLongArray("data").length == 0) {
            // Section has a uniform block
            foundBlocks.addAll(processUniformSection(chunkX, chunkZ, ySection, palette));
            return foundBlocks;
        }

        // Section has mixed blocks, use bitwise extraction
        foundBlocks.addAll(processDataArray(chunkX, chunkZ, ySection, section, palette, relevantPaletteIndices));

        return foundBlocks;
    }

    private Set<Integer> getRelevantPaletteIndices(ListTag palette) {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            String blockId = ((CompoundTag) palette.get(i)).getString("Name");
            if (idToOptionsMap.containsKey(blockId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<BlockData> processUniformSection(int chunkX, int chunkZ, int ySection, ListTag palette) {
        List<BlockData> foundBlocks = new ArrayList<>();
        String blockId = ((CompoundTag) palette.get(0)).getString("Name");

        if (!idToOptionsMap.containsKey(blockId)) return foundBlocks;

        Map<String, String> options = idToOptionsMap.get(blockId);
        int minY = options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
        int maxY = options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;

        for (int index = 0; index < 4096; index++) {
            int x = index & 0xF;
            int z = (index >> 4) & 0xF;
            int y = (index >> 8) & 0xF;

            int absoluteX = (chunkX << 4) | x;
            int absoluteZ = (chunkZ << 4) | z;
            int absoluteY = ySection | y;

            if (absoluteY >= minY && absoluteY <= maxY) {
                foundBlocks.add(new BlockData(absoluteX, absoluteY, absoluteZ, blockId));
            }
        }
        return foundBlocks;
    }

    private List<BlockData> processDataArray(int chunkX, int chunkZ, int ySection, CompoundTag section, ListTag palette, Set<Integer> relevantPaletteIndices) {
        List<BlockData> foundBlocks = new ArrayList<>();
        long[] dataArray = section.getCompound("block_states").getLongArray("data");

        int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
        int blocksPerLong = 64 / bitsPerBlock;
        int mask = (1 << bitsPerBlock) - 1;

        for (int index = 0; index < 4096; index++) {
            int paletteIndex = extractPaletteIndex(dataArray, index, bitsPerBlock, blocksPerLong, mask);
            if (!relevantPaletteIndices.contains(paletteIndex)) continue;

            String blockId = ((CompoundTag) palette.get(paletteIndex)).getString("Name");
            Map<String, String> options = idToOptionsMap.get(blockId);

            int x = index & 0xF;
            int z = (index >> 4) & 0xF;
            int y = (index >> 8) & 0xF;

            int absoluteX = (chunkX << 4) | x;
            int absoluteY = ySection | y;
            int absoluteZ = (chunkZ << 4) | z;

            int minY = options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
            int maxY = options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;
            if (absoluteY < minY || absoluteY > maxY) continue;

            foundBlocks.add(new BlockData(absoluteX, absoluteY, absoluteZ, blockId));
        }
        return foundBlocks;
    }

    private int extractPaletteIndex(long[] dataArray, int index, int bitsPerBlock, int blocksPerLong, int mask) {
        int longIndex = index / blocksPerLong;
        int bitOffset = (index % blocksPerLong) * bitsPerBlock;
        if (longIndex >= dataArray.length) return 0;
        return (int) ((dataArray[longIndex] >> bitOffset) & mask);
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max", "type");
    }

    @Override
    public String getDescription() {
        return "Find all blocks matching. Usage: id=<id>,option=<option>.";
    }
}
