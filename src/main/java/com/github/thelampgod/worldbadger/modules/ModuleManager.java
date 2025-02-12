package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.modules.impl.SignModule;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModuleManager {
    private Set<SearchModule> modules = new HashSet<>();

    public ModuleManager() {
        modules.add(new SignModule());
    }

    public Optional<SearchModule> findModule(String name) {
        return modules.stream()
                .filter(module -> module.getName().startsWith(name))
                .findAny();
    }

    public List<SearchModule> getEnabledModules() {
        return modules.stream()
                .filter(SearchModule::isToggled)
                .toList();
    }

    public void processChunk(Chunk chunk) {
        if (chunk == null) return;
        getEnabledModules().forEach(module -> {
            if (module instanceof BlockEntitySearchModule mod) {
                List<CompoundTag> blockEntities = chunk.getData().getList("block_entities").stream()
                        .map(CompoundTag.class::cast)
                        .toList();

                Object returned = mod.processChunkBlockEntities(blockEntities);

                if (returned == null) return;
                System.out.println(returned);
            }

        });

    }
}
