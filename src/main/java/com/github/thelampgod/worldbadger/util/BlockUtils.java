package com.github.thelampgod.worldbadger.util;

import lombok.Getter;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.*;
import java.util.function.Predicate;

public class BlockUtils {

    /**
     * Search for blocks in a chunk based on the provided criteria
     *
     * @param chunk The chunk to search in
     * @param blockSearchCriteria Map of block IDs to their search criteria
     * @return List of found block positions and their IDs
     */
    public static List<BlockPosition> findBlocksInChunk(Chunk chunk, Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockPosition> foundBlocks = new ArrayList<>();
        var list = chunk.getData().getList("sections");

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        for (Object obj : list) {
            CompoundTag section = (CompoundTag) obj;
            int ySection = section.getByte("Y") << 4; // Convert section index to absolute Y

            // Process this section
            foundBlocks.addAll(processSection(chunkX, chunkZ, ySection, section, blockSearchCriteria));
        }

        return foundBlocks;
    }

    public static List<BlockPosition> findBlocksInChunk(Chunk chunk, Set<String> blockStates) {
        Map<String, BlockSearchCriteria> blockSearchCriteria = new HashMap<>();
        for (String blockState : blockStates) {
            blockSearchCriteria.put(blockState, new BlockSearchCriteria());
        }

        return findBlocksInChunk(chunk, blockSearchCriteria);
    }

    /**
     * Get the block ID at a specific coordinate in a chunk
     *
     * @param chunk The chunk to check
     * @param x Absolute X coordinate
     * @param y Absolute Y coordinate
     * @param z Absolute Z coordinate
     * @return The block ID at the specified coordinate, or null if not found
     */
    public static String getBlockAtCoordinate(Chunk chunk, int x, int y, int z) {
        int relX = x & 0xF;
        int relZ = z & 0xF;

        // Find the correct section for this Y coordinate
        int sectionY = y >> 4;
        var sections = chunk.getData().getList("sections");

        for (Object obj : sections) {
            CompoundTag section = (CompoundTag) obj;
            if (section.getByte("Y") == sectionY) {
                return getBlockInSection(section, y, relX, relZ);
            }
        }

        return null;
    }

    private static String getBlockInSection(CompoundTag section, int absY, int relX, int relZ) {
        if (!section.containsKey("block_states")) {
            return null;
        }

        CompoundTag blockStates = section.getCompound("block_states");
        ListTag palette = blockStates.getList("palette");

        int sectionYBase = section.getByte("Y") << 4;
        int relY = absY - sectionYBase;

        if (relY < 0 || relY > 15) {
            return null;
        }

        int index = (relY << 8) | (relZ << 4) | relX;

        if (!blockStates.containsKey("data") || blockStates.getLongArray("data").length == 0) {
            // Uniform section
            return ((CompoundTag) palette.get(0)).getString("Name");
        }

        // Mixed blocks section
        long[] dataArray = blockStates.getLongArray("data");
        int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
        int blocksPerLong = 64 / bitsPerBlock;
        int mask = (1 << bitsPerBlock) - 1;

        int paletteIndex = extractPaletteIndex(dataArray, index, bitsPerBlock, blocksPerLong, mask);
        if (paletteIndex >= palette.size()) {
            return null;
        }

        return ((CompoundTag) palette.get(paletteIndex)).getString("Name");
    }

    private static List<BlockPosition> processSection(int chunkX, int chunkZ, int ySection, CompoundTag section,
                                                      Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockPosition> foundBlocks = new ArrayList<>();

        if (!section.containsKey("block_states")) {
            // sometimes section 20 exists and has no block_state, lets just ignore this for now
            // TODO: log maybe?
            return foundBlocks;
        }

        CompoundTag blockStates = section.getCompound("block_states");
        ListTag palette = blockStates.getList("palette");

        Set<Integer> relevantPaletteIndices = getRelevantPaletteIndices(palette, blockSearchCriteria.keySet());
        if (relevantPaletteIndices.isEmpty()) return foundBlocks;

        if (!blockStates.containsKey("data") || blockStates.getLongArray("data").length == 0) {
            // Section has a uniform block
            foundBlocks.addAll(processUniformSection(chunkX, chunkZ, ySection, palette, blockSearchCriteria));
            return foundBlocks;
        }

        // Section has mixed blocks, use bitwise extraction
        foundBlocks.addAll(processDataArray(chunkX, chunkZ, ySection, section, palette, relevantPaletteIndices, blockSearchCriteria));

        return foundBlocks;
    }

    private static Set<Integer> getRelevantPaletteIndices(ListTag palette, Set<String> targetBlockIds) {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            String blockId = ((CompoundTag) palette.get(i)).getString("Name");
            if (targetBlockIds.contains(blockId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static List<BlockPosition> processUniformSection(int chunkX, int chunkZ, int ySection, ListTag palette,
                                                             Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockPosition> foundBlocks = new ArrayList<>();
        String blockId = ((CompoundTag) palette.get(0)).getString("Name");

        if (!blockSearchCriteria.containsKey(blockId)) return foundBlocks;

        BlockSearchCriteria criteria = blockSearchCriteria.get(blockId);

        for (int index = 0; index < 4096; index++) {
            int x = index & 0xF;
            int z = (index >> 4) & 0xF;
            int y = (index >> 8) & 0xF;

            int absoluteX = (chunkX << 4) | x;
            int absoluteZ = (chunkZ << 4) | z;
            int absoluteY = ySection | y;

            if (criteria.matches(absoluteY)) {
                foundBlocks.add(new BlockPosition(absoluteX, absoluteY, absoluteZ, blockId));
            }
        }
        return foundBlocks;
    }

    private static List<BlockPosition> processDataArray(int chunkX, int chunkZ, int ySection, CompoundTag section,
                                                        ListTag palette, Set<Integer> relevantPaletteIndices,
                                                        Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockPosition> foundBlocks = new ArrayList<>();
        long[] dataArray = section.getCompound("block_states").getLongArray("data");

        int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
        int blocksPerLong = 64 / bitsPerBlock;
        int mask = (1 << bitsPerBlock) - 1;

        for (int index = 0; index < 4096; index++) {
            int paletteIndex = extractPaletteIndex(dataArray, index, bitsPerBlock, blocksPerLong, mask);
            if (!relevantPaletteIndices.contains(paletteIndex)) continue;

            String blockId = ((CompoundTag) palette.get(paletteIndex)).getString("Name");
            BlockSearchCriteria criteria = blockSearchCriteria.get(blockId);

            int x = index & 0xF;
            int z = (index >> 4) & 0xF;
            int y = (index >> 8) & 0xF;

            int absoluteX = (chunkX << 4) | x;
            int absoluteY = ySection | y;
            int absoluteZ = (chunkZ << 4) | z;

            if (criteria.matches(absoluteY)) {
                foundBlocks.add(new BlockPosition(absoluteX, absoluteY, absoluteZ, blockId));
            }
        }
        return foundBlocks;
    }

    private static int extractPaletteIndex(long[] dataArray, int index, int bitsPerBlock, int blocksPerLong, int mask) {
        int longIndex = index / blocksPerLong;
        int bitOffset = (index % blocksPerLong) * bitsPerBlock;
        if (longIndex >= dataArray.length) return 0;
        return (int) ((dataArray[longIndex] >> bitOffset) & mask);
    }

    /**
     * Represents a block position with its ID
     */
    @Getter
    public static class BlockPosition {
        private final int x;
        private final int y;
        private final int z;
        private final String blockId;

        public BlockPosition(int x, int y, int z, String blockId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
        }

    }

    /**
     * Criteria for searching blocks
     */
    public static class BlockSearchCriteria {
        private final int minY;
        private final int maxY;
        private final Predicate<Integer> customCondition;

        public BlockSearchCriteria(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
            this.customCondition = null;
        }

        public BlockSearchCriteria(Predicate<Integer> customCondition) {
            this.minY = Integer.MIN_VALUE;
            this.maxY = Integer.MAX_VALUE;
            this.customCondition = customCondition;
        }

        public BlockSearchCriteria() {
            this(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public boolean matches(int y) {
            if (customCondition != null) {
                return customCondition.test(y);
            }
            return y >= minY && y <= maxY;
        }
    }
}