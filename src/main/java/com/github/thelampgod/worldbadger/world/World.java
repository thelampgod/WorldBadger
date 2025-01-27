package com.github.thelampgod.worldbadger.world;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import lombok.Getter;
import net.querz.mca.MCAFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class World {
    private List<MCAFile> regions = new ArrayList<>();
    private List<MCAFile> entities = new ArrayList<>();

    private final Path worldRoot;

    public World(String worldPath, int dimension) throws IOException {
        this.worldRoot = Path.of(worldPath);
        findRegions(dimension);
    }

    private void findRegions(int dimension) throws IOException {
        Path dimensionRoot = worldRoot;
        if (dimension != 0) {
            dimensionRoot = worldRoot.resolve("DIM" + dimension);
        }

        Path regionDir = dimensionRoot.resolve("region");
        Path entitiesDir = dimensionRoot.resolve("entities");

        this.regions = getRegions(regionDir);
        this.entities = getRegions(entitiesDir);
    }

    private List<MCAFile> getRegions(Path regionDir) throws IOException {
        List<MCAFile> temp = new ArrayList<>();

        if (!Files.exists(regionDir)) {
            return temp;
        }

        try (var stream = Files.list(regionDir)) {
            stream
                    .filter(path -> path.endsWith(".mca"))
                    .filter(path -> {
                        try {
                            return Files.size(path) > 0;
                        } catch (IOException e) {
                            System.err.println("error with " + path.toString());
                            return false;
                        }
                    })
                    .map(Path::toFile)
                    .map(MCAFile::new)
                    .forEach(temp::add);
        }


        return temp;
    }
}
