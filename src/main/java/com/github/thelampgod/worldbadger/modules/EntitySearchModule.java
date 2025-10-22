package com.github.thelampgod.worldbadger.modules;

import net.querz.nbt.CompoundTag;

import java.util.List;

public abstract class EntitySearchModule extends SearchModule {


    public EntitySearchModule(String name) {
        super(name);
    }

    public abstract List<?> processEntities(List<CompoundTag> entities);
}
