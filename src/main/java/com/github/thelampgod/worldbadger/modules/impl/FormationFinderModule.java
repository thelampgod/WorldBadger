package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.util.blocks.BlockState;
import com.github.thelampgod.worldbadger.util.blocks.BlockUtils;
import lombok.Data;
import net.querz.mca.Chunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FormationFinderModule extends SearchModule {

    private List<BlockState> formation;
    private final WorldBadger main;

    public FormationFinderModule(WorldBadger instance) {
        super("formation-finder");
        this.main = instance;
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {
        if (formation.isEmpty()) return List.of();

        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        for (BlockState state : formation) {
            int absX = chunkX + state.getX();
            int absZ = chunkZ + state.getZ();
            var found = BlockUtils.getBlockAtCoordinate(chunk, absX, state.getY(), absZ);
            if (found == null || !state.getId().equals(found.getId())) {
                // No match, immediately return
                return List.of();
            }
        }

        // All blocks in the formation matches, return result!

        // Should we exit after?
        if (idToOptionsMap.get("all").containsKey("exit-immediately")) {
            main.getWorldManager().stopSearch();
        }

        final BlockState pos = formation.get(0);
        return List.of(new MatchData(chunkX + pos.getX(), pos.getY(), chunkZ + pos.getZ()));
    }

    @Override
    public void options(String[] args) {
        super.options(args);
        if (!this.idToOptionsMap.containsKey("all") || !this.idToOptionsMap.get("all").containsKey("config")) {
            throw new NoSuchElementException("Missing required arg 'config'");
        }

        try {
            Path file = Path.of(this.idToOptionsMap.get("all").get("config"));
            this.formation = Files.readAllLines(file).stream()
                    .map(line -> {
                        String[] parts = line.split(",");
                        int x = Integer.parseInt(parts[0]) & 15;
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]) & 15;
                        String blockId = parts[3];

                        if (y < -64 || y > 319) {
                            throw new IllegalArgumentException("Y coordinate " + y + " out of range (-64 to 319)");
                        }

                        return new BlockState(x,y,z,blockId, Map.of());
                    }).toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid file. File should be in format `x,y,z,blockId` for each line");
        }
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("config", "exit-immediately");
    }

    @Override
    public boolean requiresId() {
        return false;
    }

    @Override
    public String getDescription() {
        return """
                Searches the world for the provided block formation.
                    Use chunk relative coordinates, currently only works chunkwise so chunk borders need to be known.
                    Attach a config file in the following format per line: "x,y,z,blockId", for example 0,63,15,minecraft:coal_ore.""";
    }

    @Data
    private static class MatchData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z);
        }
    }
}
