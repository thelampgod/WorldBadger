package com.github.thelampgod.worldbadger.world;

import com.github.thelampgod.worldbadger.WorldBadger;
import lombok.Getter;

import java.io.IOException;

public class WorldManager {
    private final WorldBadger main;

    @Getter
    private World world;

    public WorldManager(WorldBadger instance) {
        this.main = instance;
    }

    public World setWorld(String worldPath, int dimension) throws IOException {
        this.world = new World(worldPath, dimension);
        return world;
    }

    //TODO: check enabled modules of which type of region is being searched (dont need to load "entities" regions for a sign search)
    public void startSearch() {
        world.getRegions().parallelStream()
                .forEach(region -> {
                    try {
                        region.load();
                        region.forEach(chunk -> main.getModuleManager().processChunk(chunk));

                    } catch (IOException e) {
                        System.err.println("Failed to load region " + region.getName());
                    } finally {
                        region.unload();
                    }
                });
    }
}
