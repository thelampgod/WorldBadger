package com.github.thelampgod.worldbadger.util.blocks;

import com.github.thelampgod.worldbadger.util.Helper;
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
     * @return List of found block states and their positions
     */
    public static List<BlockState> findBlocksInChunk(Chunk chunk, Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockState> foundBlocks = new ArrayList<>();
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

    public static List<BlockState> findBlocksInChunk(Chunk chunk, Set<String> blockStateIds) {
        Map<String, BlockSearchCriteria> blockSearchCriteria = new HashMap<>();
        for (String blockStateId : blockStateIds) {
            // add namespace if missing
            if (!blockStateId.startsWith("minecraft:")) {
                blockStateId = "minecraft:" + blockStateId;
            }

            blockSearchCriteria.put(blockStateId, new BlockSearchCriteria());
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
     * @return The block state at the specified coordinate, or null if not found
     */
    public static BlockState getBlockAtCoordinate(Chunk chunk, int x, int y, int z) {
        int relX = x & 0xF;
        int relZ = z & 0xF;

        // Find the correct section for this Y coordinate
        int sectionY = y >> 4;
        var sections = chunk.getData().getList("sections");

        for (Object obj : sections) {
            CompoundTag section = (CompoundTag) obj;
            if (section.getByte("Y") == sectionY) {
                return getBlockInSection(section, x, y, z, relX, relZ);
            }
        }

        return null;
    }

    private static BlockState getBlockInSection(CompoundTag section, int absX, int absY, int absZ, int relX, int relZ) {
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
            CompoundTag tag = ((CompoundTag) palette.get(0));

            return extractBlockState(tag, absX, absY, absZ);
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

        CompoundTag tag = ((CompoundTag) palette.get(paletteIndex));

        return extractBlockState(tag, absX, absY, absZ);
    }

    private static BlockState extractBlockState(CompoundTag tag, int absX, int absY, int absZ) {
        String blockId = tag.getString("Name");
        List<Property> properties = new ArrayList<>();

        if (tag.containsKey("Properties")) {
            tag.getCompound("Properties").forEach(prop -> {
                properties.add(new Property(prop.getKey(), Helper.SNBT_WRITER.toString(prop.getValue())));
            });
        }

        return new BlockState(absX, absY, absZ, blockId, properties);
    }

    private static List<BlockState> processSection(int chunkX, int chunkZ, int ySection, CompoundTag section,
                                                      Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockState> foundBlocks = new ArrayList<>();

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

    private static List<BlockState> processUniformSection(int chunkX, int chunkZ, int ySection, ListTag palette,
                                                             Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockState> foundBlocks = new ArrayList<>();
        CompoundTag tag = ((CompoundTag) palette.get(0));
        String blockId = tag.getString("Name");

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
                foundBlocks.add(extractBlockState(tag, absoluteX, absoluteY, absoluteZ));
            }
        }
        return foundBlocks;
    }

    private static List<BlockState> processDataArray(int chunkX, int chunkZ, int ySection, CompoundTag section,
                                                        ListTag palette, Set<Integer> relevantPaletteIndices,
                                                        Map<String, BlockSearchCriteria> blockSearchCriteria) {
        List<BlockState> foundBlocks = new ArrayList<>();
        long[] dataArray = section.getCompound("block_states").getLongArray("data");

        int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
        int blocksPerLong = 64 / bitsPerBlock;
        int mask = (1 << bitsPerBlock) - 1;

        for (int index = 0; index < 4096; index++) {
            int paletteIndex = extractPaletteIndex(dataArray, index, bitsPerBlock, blocksPerLong, mask);
            if (!relevantPaletteIndices.contains(paletteIndex)) continue;

            CompoundTag tag = ((CompoundTag) palette.get(paletteIndex));
            String blockId = tag.getString("Name");
            BlockSearchCriteria criteria = blockSearchCriteria.get(blockId);

            int x = index & 0xF;
            int z = (index >> 4) & 0xF;
            int y = (index >> 8) & 0xF;

            int absoluteX = (chunkX << 4) | x;
            int absoluteY = ySection | y;
            int absoluteZ = (chunkZ << 4) | z;

            if (criteria.matches(absoluteY)) {
                foundBlocks.add(extractBlockState(tag, absoluteX, absoluteY, absoluteZ));
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