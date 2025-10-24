package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.output.DataClass;
import net.querz.nbt.CompoundTag;

import java.util.List;

public abstract class BlockEntitySearchModule extends SearchModule {

    public BlockEntitySearchModule(String name) {
        super(name);
    }

    public abstract List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities);
}
