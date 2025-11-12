package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.util.blocks.BlockEntityMapping;
import com.github.thelampgod.worldbadger.util.blocks.BlockState;
import com.github.thelampgod.worldbadger.util.blocks.BlockUtils;
import lombok.Data;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DatalessModule extends SearchModule {
    public DatalessModule() {
        super("dataless");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        List<DatalessResult> results = new ArrayList<>();
        // find block entities in block state form
        final Set<String> blockStates =  BlockEntityMapping.getAllBlocksWithBlockEntities();
        final List<BlockState> blockStateEntities = BlockUtils.findBlocksInChunk(chunk, blockStates);

        // get the stored block entities
        final List<CompoundTag> blockEntities = chunk.getData().getList("block_entities").stream()
                .map(CompoundTag.class::cast)
                .toList();

        results.addAll(checkBlockStates(blockStateEntities, blockEntities));

        results.addAll(checkBlockEntities(blockEntities, blockStateEntities, chunk));

        return results;
    }

    /**
     * Find block entities that do not exist in block form
     * @param blockEntities chunk block entities
     * @param blockStateEntities chunk block states that should have a block entity
     * @param chunk
     * @return list of results
     */
    private Collection<? extends DatalessResult> checkBlockEntities(List<CompoundTag> blockEntities, List<BlockState> blockStateEntities, Chunk chunk) {
        final List<DatalessResult> results = new ArrayList<>();
        blockEntityLoop:
        for (CompoundTag blockEntity : blockEntities) {
            int x = blockEntity.getInt("x");
            int y = blockEntity.getInt("y");
            int z = blockEntity.getInt("z");
            String blockEntityId = blockEntity.getString("id");

            // Handle minecraft: namespace if present
            if (blockEntityId.startsWith("minecraft:")) {
                blockEntityId = blockEntityId.substring(10);
            }

            for (BlockState blockState : blockStateEntities) {
                if (blockState.positionMatches(x,y,z)) {
                    if (BlockEntityMapping.isValidBlockForBlockEntity(blockEntityId, blockState.getId())) {
                        // block entity exists in block state form, all good
                        continue blockEntityLoop;
                    }
                    // there's a different block entity block state in this block entities position, log
                    results.add(new DatalessResult(x,y,z, "BLOCK_ENTITY_WITH_MISMATCHED_BLOCK", blockEntityId, blockState.getId()));
                    continue blockEntityLoop;
                }
            }
            // no matching block state for the block entity found, log
            final BlockState actual = BlockUtils.getBlockAtCoordinate(chunk, x,y,z);
            results.add(new DatalessResult(x,y,z, "BLOCK_ENTITY_WITHOUT_BLOCK", blockEntityId, (actual == null ? "null" : actual.getId())));
        }
        return results;
    }

    /**
     *  Find block state block entities that do not exist in block entity form
     * @param blockStateEntities chunk block states that should have a block entity
     * @param blockEntities chunk block entities
     * @return list of results
     */
    private List<DatalessResult> checkBlockStates(final List<BlockState> blockStateEntities, final List<CompoundTag> blockEntities) {
        final List<DatalessResult> results = new ArrayList<>();
        blockStatesLoop:
        for (BlockState blockState : blockStateEntities) {
            String expectedBlockEntity = BlockEntityMapping.getBlockEntityForBlock(blockState.getId());
            // check if it exists in the block entity list
            for (CompoundTag blockEntity : blockEntities) {
                int x = blockEntity.getInt("x");
                int y = blockEntity.getInt("y");
                int z = blockEntity.getInt("z");
                String blockEntityId = blockEntity.getString("id");

                // Handle minecraft: namespace if present
                if (blockEntityId.startsWith("minecraft:")) {
                    blockEntityId = blockEntityId.substring(10);
                }

                if (blockState.positionMatches(x,y,z)) {
                    if (expectedBlockEntity.equals(blockEntityId)) {
                        // block state exists in block entity form, all good
                        continue blockStatesLoop;
                    }
                    // there's a different block entity in this block states position, log
                    results.add(new DatalessResult(x,y,z, "BLOCK_WITH_MISMATCHED_BLOCK_ENTITY", expectedBlockEntity, blockEntityId));
                    continue blockStatesLoop;
                }
            }
            // block state doesn't exist in the block entity form -> log it
            results.add(new DatalessResult(
                    blockState.getX(),
                    blockState.getY(),
                    blockState.getZ(),
                    "BLOCK_WITHOUT_BLOCK_ENTITY", blockState.getId(), "null")
            );
        }

        return results;
    }

    @Data
    private static class DatalessResult implements DataClass {
        private final int x;
        private final int y;
        private final int z;
        private final String issueType; // "BLOCK_ENTITY_WITHOUT_BLOCK" or "BLOCK_WITHOUT_BLOCK_ENTITY"
        private final String expectedId; // block state id or block entity id
        private final String actualId; // block state id or block entity id


        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "issueType", "expectedId", "actualId");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, issueType, expectedId, actualId);
        }
    }

    @Override
    public String getDescription() {
        return "Find all block entities that do not exist in block form & vice versa.";
    }
}
