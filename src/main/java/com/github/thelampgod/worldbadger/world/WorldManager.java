package com.github.thelampgod.worldbadger.world;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import com.github.thelampgod.worldbadger.util.ProgressBar;
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
        long start = System.currentTimeMillis();

        boolean shouldSearchRegions = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> !(module instanceof EntitySearchModule));
        boolean shouldSearchEntities = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> (module instanceof EntitySearchModule));

        int total = (shouldSearchRegions ? world.getRegions().size() : 0) + (shouldSearchEntities ? world.getEntities().size() : 0);
        ProgressBar progress = new ProgressBar(total, main.getOutputMode());
        new Thread(() -> {
            while (progress.shouldRun()) {
                try {
                    progress.printProgressBar();
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }).start();

        if (shouldSearchRegions) {
            world.getRegions().parallelStream()
                    .forEach(region -> {
                        try {
                            region.load();
                            region.forEach(chunk -> main.getModuleManager().processChunk(chunk));
                            progress.increment();
                        } catch (IOException e) {
                            main.logger.error("Failed to load region {}", region.getName());
                        } finally {
                            region.unload();
                        }
                    });
        }


        if (shouldSearchEntities) {
            world.getEntities().parallelStream()
                    .forEach(region -> {
                        try {
                            region.load();
                            region.forEach(chunk -> main.getModuleManager().processEntities(chunk));
                            progress.increment();
                        } catch (IOException e) {
                            main.logger.error("Failed to load region {}", region.getName());
                        } finally {
                            region.unload();
                        }
                    });
        }

        progress.close();
        System.out.println();
        main.logger.info("Search finished in {}ms", System.currentTimeMillis() - start);
        main.getOutputMode().close();
    }
}
