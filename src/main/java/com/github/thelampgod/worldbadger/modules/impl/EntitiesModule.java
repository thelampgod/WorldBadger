package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import net.querz.nbt.CompoundTag;

import java.util.List;

public class EntitiesModule extends EntitySearchModule {

    public EntitiesModule() {
        super("entities");
    }

    @Override
    public Object processEntities(List<CompoundTag> entities) {
        return null;
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max");
    }
}
