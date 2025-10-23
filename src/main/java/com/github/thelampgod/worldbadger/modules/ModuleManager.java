package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.impl.BlockModule;
import com.github.thelampgod.worldbadger.modules.impl.EntityModule;
import com.github.thelampgod.worldbadger.modules.impl.SignModule;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModuleManager {
    private final Set<SearchModule> modules = new HashSet<>();

    private final WorldBadger instance;
    public ModuleManager(WorldBadger instance) {
        this.instance = instance;
        modules.add(new SignModule());
        modules.add(new BlockModule());
        modules.add(new EntityModule());
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

                var ret = mod.processChunkBlockEntities(blockEntities);

                instance.getOutputMode().processChunkResult(mod.getName(), ret);
                return;
            }

            var ret = module.processChunk(chunk);
            instance.getOutputMode().processChunkResult(module.getName(), ret);
        });
    }

    public void processEntities(Chunk chunk) {
        if (chunk == null) return;
        getEnabledModules().stream()
                .filter(EntitySearchModule.class::isInstance)
                .map(EntitySearchModule.class::cast)
                .forEach(module -> {
                    List<CompoundTag> entities = chunk.getData().getList("Entities").stream()
                            .map(CompoundTag.class::cast)
                            .toList();

                    var ret = module.processEntities(entities);
                    instance.getOutputMode().processChunkResult(module.getName(), ret);
                });
    }
}
