package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.util.blocks.BlockState;
import com.github.thelampgod.worldbadger.util.blocks.BlockUtils;
import com.github.thelampgod.worldbadger.util.vectors.Vec3i;
import lombok.Data;
import net.querz.mca.Chunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HalfDoorModule extends SearchModule {

    final Set<String> doors = Set.of("iron_door", "oak_door", "birch_door", "spruce_door", "jungle_door", "dark_oak_door",
            "acacia_door", "mangrove_door", "cherry_door", "bamboo_door", "pale_oak_door", "warped_door",
            "crimson_door", "copper_door", "exposed_copper_door", "weathered_copper_door", "oxidized_copper_door",
            "waxed_copper_door", "waxed_exposed_copper_door", "waxed_weathered_copper_door", "waxed_oxidized_copper_door");

    public HalfDoorModule() {
        super("half-door");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        final List<HalfDoorData> results = new ArrayList<>();
        final Set<Vec3i> checkedPositions = new HashSet<>();

        final List<BlockState> foundDoors = BlockUtils.findBlocksInChunk(chunk, doors);

        for (BlockState door : foundDoors) {
            final Vec3i currentPos = new Vec3i(door.getX(), door.getY(), door.getZ());

            if (checkedPositions.contains(currentPos)) continue;

            boolean bottomHalf = door.getProperties().get("half").equals("lower");
            Vec3i otherPos = new Vec3i(door.getX(), door.getY() + (bottomHalf ? 1 : -1), door.getZ());
            BlockState otherHalf = BlockUtils.getBlockAtCoordinate(chunk, otherPos.getX(), otherPos.getY(), otherPos.getZ());

            if (otherHalf == null || !door.getId().equals(otherHalf.getId())) {
                results.add(new HalfDoorData(door.getX(), door.getY(), door.getZ(), door.getId(), (bottomHalf) ? "lower" : "upper"));

                checkedPositions.add(currentPos);
                checkedPositions.add(otherPos);
                continue;
            }

            boolean otherBottomHalf = otherHalf.getProperties().get("half").equals("lower");

            // if both halves are the same type, log as well
            if (bottomHalf == otherBottomHalf) {
                results.add(new HalfDoorData(door.getX(), door.getY(), door.getZ(), door.getId(), (bottomHalf) ? "lower" : "upper"));

                checkedPositions.add(currentPos);
                checkedPositions.add(otherPos);
            }
        }

        return results;
    }

    @Data
    private static class HalfDoorData implements DataClass {
        private final int x;
        private final int y;
        private final int z;
        private final String doorType;
        private final String existingHalf;


        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "doorType", "existingHalf");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, doorType, existingHalf);
        }
    }

    @Override
    public String getDescription() {
        return "Finds doors that lack a top or bottom half";
    }
}
