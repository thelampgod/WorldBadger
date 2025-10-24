package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.output.DataClass;
import net.querz.nbt.CompoundTag;

import java.util.List;

public abstract class EntitySearchModule extends SearchModule {


    public EntitySearchModule(String name) {
        super(name);
    }

    public abstract List<? extends DataClass> processEntities(List<CompoundTag> entities);
}
