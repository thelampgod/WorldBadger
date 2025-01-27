package com.github.thelampgod.worldbadger.world;

import net.querz.mca.MCAFile;

import java.io.File;
import java.lang.reflect.Field;

public class UnloadableMcaFile extends MCAFile {
    public UnloadableMcaFile(File file) {
        super(file);
    }

    public void unload() {
        try {
            Field chunksField = MCAFile.class.getDeclaredField("chunks");
            chunksField.setAccessible(true);
            chunksField.set(this, null);
        } catch (Exception e) {
            System.err.println("Failed to unload chunks: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
