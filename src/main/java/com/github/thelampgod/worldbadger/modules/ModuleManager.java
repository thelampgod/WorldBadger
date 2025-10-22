package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.modules.impl.BlockModule;
import com.github.thelampgod.worldbadger.modules.impl.EntitiesModule;
import com.github.thelampgod.worldbadger.modules.impl.SignModule;
import com.github.thelampgod.worldbadger.output.OutputMode;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModuleManager {
    private final Set<SearchModule> modules = new HashSet<>();

    private final OutputMode outputMode;
    public ModuleManager(OutputMode outputMode) {
        this.outputMode = outputMode;
        modules.add(new SignModule());
        modules.add(new BlockModule());
        modules.add(new EntitiesModule());
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

                outputMode.processChunkResult(mod.getName(), ret);
                return;
            }

            var ret = module.processChunk(chunk);
            outputMode.processChunkResult(module.getName(), ret);
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
                    outputMode.processChunkResult(module.getName(), ret);
                });
    }
}
