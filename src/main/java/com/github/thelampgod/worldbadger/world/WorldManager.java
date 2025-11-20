package com.github.thelampgod.worldbadger.world;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import com.github.thelampgod.worldbadger.util.ProgressBar;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    private ExecutorService executorService;
    private volatile boolean shouldStop = false;
    private List<Future<?>> activeTasks;

    public void startSearch() throws Exception {
        shouldStop = false;
        activeTasks = new ArrayList<>();

        main.getOutputMode().initialize(this.outputFolder);
        long start = System.currentTimeMillis();

        boolean shouldSearchRegions = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> !(module instanceof EntitySearchModule));
        boolean shouldSearchEntities = main.getModuleManager().getEnabledModules().stream()
                .anyMatch(module -> (module instanceof EntitySearchModule));

        int total = (shouldSearchRegions ? world.getRegions().size() : 0) + (shouldSearchEntities ? world.getEntities().size() : 0);
        ProgressBar progress = new ProgressBar(total, main.getOutputMode());

        Thread progressThread = new Thread(() -> {
            while (progress.shouldRun() && !shouldStop) {
                try {
                    progress.printProgressBar();
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        });
        progressThread.start();

        // Create executor service with optimal thread count
        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        executorService = Executors.newFixedThreadPool(threadCount);

        try {
            // Process regions
            if (shouldSearchRegions && !shouldStop) {
                for (UnloadableMcaFile region : world.getRegions()) {
                    if (shouldStop) break;
                    Future<?> future = executorService.submit(() -> processRegion(region, progress, false));
                    activeTasks.add(future);
                }
            }

            // Process entities
            if (shouldSearchEntities && !shouldStop) {
                for (UnloadableMcaFile region : world.getEntities()) {
                    if (shouldStop) break;
                    Future<?> future = executorService.submit(() -> processRegion(region, progress, true));
                    activeTasks.add(future);
                }
            }

            // Wait for all tasks to complete or be cancelled
            for (Future<?> future : activeTasks) {
                if (shouldStop) break;
                try {
                    future.get();
                } catch (CancellationException | InterruptedException e) {
                    shouldStop = true;
                    break;
                } catch (ExecutionException e) {
                    main.logger.error("Task execution failed", e.getCause());
                }
            }

        } finally {
            // Ensure executor service is shut down
            executorService.shutdownNow();

            // Wait for progress thread to finish
            try {
                progressThread.join(1000);
            } catch (InterruptedException e) {
                progressThread.interrupt();
            }

            // print newline for progress bar
            System.out.println();
            progress.close();

            if (shouldStop) {
                main.logger.info("Search stopped by user after {}ms", System.currentTimeMillis() - start);
            } else {
                main.logger.info("Search finished in {}ms", System.currentTimeMillis() - start);
            }
            main.getOutputMode().close();
        }
    }

    private void processRegion(UnloadableMcaFile region, ProgressBar progress, boolean isEntityRegion) {
        if (shouldStop) return;

        try {
            region.load();

            if (isEntityRegion) {
                region.forEach(chunk -> {
                    if (shouldStop) return;
                    main.getModuleManager().processEntities(chunk);
                });
            } else {
                region.forEach(chunk -> {
                    if (shouldStop) return;
                    main.getModuleManager().processChunk(chunk);
                });
            }

            progress.increment();

        } catch (IOException e) {
            main.logger.error("Failed to load region {}", region.getName());
        } finally {
            region.unload();
        }
    }

    public void stopSearch() {
        shouldStop = true;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (activeTasks != null) {
            activeTasks.forEach(future -> future.cancel(true));
        }
    }

    public boolean isSearchRunning() {
        return executorService != null && !executorService.isTerminated();
    }
}
