package com.github.thelampgod.worldbadger.world;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

public class WorldManager {
    private final WorldBadger main;

    @Getter
    private World world;

    private Path outputFolder = Path.of("./output/");

    public WorldManager(WorldBadger instance) {
        this.main = instance;
    }

    public World setWorld(String worldPath, int dimension) throws IOException {
        this.world = new World(worldPath, dimension);
        return world;
    }

    public void setOutputFolder(String output) {
        this.outputFolder = Path.of(output);
    }

    public void startSearch() throws Exception {
        main.getOutputMode().initialize(this.outputFolder);
        boolean shouldSearchRegions = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> !(module instanceof EntitySearchModule));
        if (shouldSearchRegions) {
            world.getRegions().parallelStream()
                    .forEach(region -> {
                        try {
                            region.load();
                            region.forEach(chunk -> main.getModuleManager().processChunk(chunk));

                        } catch (IOException e) {
                            main.logger.error("Failed to load region {}", region.getName());
                        } finally {
                            region.unload();
                        }
                    });
        }

        boolean shouldSearchEntities = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> (module instanceof EntitySearchModule));

        if (shouldSearchEntities) {
            world.getEntities().parallelStream()
                    .forEach(region -> {
                        try {
                            region.load();
                            region.forEach(chunk -> main.getModuleManager().processEntities(chunk));

                        } catch (IOException e) {
                            main.logger.error("Failed to load region {}", region.getName());
                        } finally {
                            region.unload();
                        }
                    });
        }

        main.getOutputMode().close();
    }
}
