package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.util.blocks.BlockState;
import com.github.thelampgod.worldbadger.util.blocks.BlockUtils;
import lombok.Data;
import net.querz.mca.Chunk;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockModule extends SearchModule {

    public BlockModule() {
        super("block");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        // Convert the module's idToOptionsMap to BlockSearchCriteria
        Map<String, BlockUtils.BlockSearchCriteria> searchCriteria = idToOptionsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> createCriteriaFromOptions(entry.getValue())
                ));

        List<BlockState> foundBlockStates = BlockUtils.findBlocksInChunk(chunk, searchCriteria);

        return foundBlockStates.stream()
                .map(state -> new BlockData(state.getX(), state.getY(), state.getZ(), state.getId()))
                .collect(Collectors.toList());
    }

    private BlockUtils.BlockSearchCriteria createCriteriaFromOptions(Map<String, String> options) {
        if (options.containsKey("min") || options.containsKey("max")) {
            int minY = options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
            int maxY = options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;
            return new BlockUtils.BlockSearchCriteria(minY, maxY);
        }
        return new BlockUtils.BlockSearchCriteria();
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

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max", "type");
    }

    @Override
    public String getDescription() {
        return "Find all blocks matching. Usage: id=<id>,option=<option>.";
    }
}