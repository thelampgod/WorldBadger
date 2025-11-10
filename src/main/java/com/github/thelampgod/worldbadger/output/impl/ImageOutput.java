package com.github.thelampgod.worldbadger.output.impl;

import com.github.thelampgod.worldbadger.output.DataClass;
import com.github.thelampgod.worldbadger.output.OutputMode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps the results to a 2D image.
 * x and z values are extracted and printed to a BufferedImage
 */
public class ImageOutput implements OutputMode {

    private int minX, minZ, maxX, maxZ;
    private boolean boundsSet = false;
    private double scaleFactor = 1.0;
    private final int MAX_DIMENSION = 32767;

    private Path outputFolder;

    private final Map<String, BufferedImage> moduleToImageMap = new ConcurrentHashMap<>();

    public void setBounds(int minX, int minZ, int maxX, int maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.boundsSet = true;

        // Calculate scale factor to fit within MAX_DIMENSION
        int worldWidth = maxX - minX + 1;
        int worldHeight = maxZ - minZ + 1;

        this.scaleFactor = Math.min(
                (double)MAX_DIMENSION / worldWidth,
                (double)MAX_DIMENSION / worldHeight
        );

        // Don't scale up, only scale down
        if (this.scaleFactor > 1.0) {
            this.scaleFactor = 1.0;
        }

        System.out.printf("Image scaling: %dx%d world -> scale %.4f%n",
                worldWidth, worldHeight, scaleFactor);
    }

    @Override
    public void initialize(Path outputFolder) throws Exception {
        if (outputFolder.toFile().exists()) {
            throw new Exception(outputFolder.toString() + " exists! Delete the folder or choose a different output.");
        }

        Files.createDirectories(outputFolder);
        this.outputFolder = outputFolder;
    }

    @Override
    public void processChunkResult(String moduleName, List<? extends DataClass> results) {
        if (results == null || results.isEmpty()) return;
        if (!boundsSet) {
            throw new IllegalArgumentException("Bounds not set.");
        }

        boolean isChunkCoord = results.get(0).getFieldNames().contains("chunkX");

        BufferedImage image = moduleToImageMap.computeIfAbsent(moduleName, name -> {
            if (isChunkCoord) {
                int chunkWidth = ((maxX - minX) >> 4) + 1;
                int chunkHeight = ((maxZ - minZ) >> 4) + 1;
                int scaledWidth = (int)(chunkWidth * scaleFactor);
                int scaledHeight = (int)(chunkHeight * scaleFactor);
                return new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_BINARY);
            }

            int blockWidth = maxX - minX + 1;
            int blockHeight = maxZ - minZ + 1;
            int scaledWidth = (int)(blockWidth * scaleFactor);
            int scaledHeight = (int)(blockHeight * scaleFactor);
            return new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_BINARY);
        });
        WritableRaster raster = image.getRaster();

        for (DataClass result : results) {
            if (isChunkCoord) {
                int minChunkX = minX >> 4;
                int minChunkZ = minZ >> 4;

                int x = ((Number)result.getFieldValues().get(result.getFieldNames().indexOf("chunkX"))).intValue();
                int y = ((Number)result.getFieldValues().get(result.getFieldNames().indexOf("chunkZ"))).intValue();

                // Apply scaling to coordinates
                int scaledX = (int)((x - minChunkX) * scaleFactor);
                int scaledY = (int)((y - minChunkZ) * scaleFactor);

                if (scaledX >= 0 && scaledX < image.getWidth() && scaledY >= 0 && scaledY < image.getHeight()) {
                    raster.setSample(scaledX, scaledY, 0, 1);
                }
                continue;
            }

            int x = ((Number)result.getFieldValues().get(result.getFieldNames().indexOf("x"))).intValue();
            int y = ((Number)result.getFieldValues().get(result.getFieldNames().indexOf("z"))).intValue();

            // Apply scaling to coordinates
            int scaledX = (int)((x - minX) * scaleFactor);
            int scaledY = (int)((y - minZ) * scaleFactor);

            if (scaledX >= 0 && scaledX < image.getWidth() && scaledY >= 0 && scaledY < image.getHeight()) {
                raster.setSample(scaledX, scaledY, 0, 1);
            }
        }
    }

    @Override
    public void close() {
        // Save all images
        for (Map.Entry<String, BufferedImage> entry : moduleToImageMap.entrySet()) {
            String moduleName = entry.getKey();
            BufferedImage image = entry.getValue();

            try {
                Path imagePath = outputFolder.resolve(moduleName + "_map.png");
                ImageIO.write(image, "PNG", imagePath.toFile());
                System.out.println("Saved image for " + moduleName + " to: " + imagePath);
                System.out.println("Final image size: " + image.getWidth() + "x" + image.getHeight());
            } catch (IOException e) {
                System.err.println("Failed to save image for " + moduleName + ": " + e.getMessage());
            }
        }

        moduleToImageMap.clear();
    }
}