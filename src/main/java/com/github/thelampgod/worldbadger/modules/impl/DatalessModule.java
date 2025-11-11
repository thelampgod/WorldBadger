package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.SearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import net.querz.mca.Chunk;

import java.util.List;

public class DatalessModule extends SearchModule {
    public DatalessModule() {
        super("dataless");
    }

    @Override
    public List<? extends DataClass> processChunk(Chunk chunk) {

    }

    @Override
    public String getDescription() {
        return "Find all block entities that do not exist in block form & vice versa.";
    }
}
