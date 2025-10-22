package com.github.thelampgod.worldbadger.modules;

import net.querz.nbt.CompoundTag;

import java.util.List;

public abstract class BlockEntitySearchModule extends SearchModule {

    public BlockEntitySearchModule(String name) {
        super(name);
    }

    public abstract List<?> processChunkBlockEntities(List<CompoundTag> blockEntities);
}
