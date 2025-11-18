package com.github.thelampgod.worldbadger.modules;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.modules.impl.*;
import lombok.Getter;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;

import java.util.*;

public class ModuleManager {
    @Getter
    private final Map<String, SearchModule> moduleMap = new HashMap<>();

    private final WorldBadger instance;

    public ModuleManager(WorldBadger instance) {
        this.instance = instance;
        put("sign", new SignModule());
        put("block", new BlockModule());
        put("entity", new EntityModule());
        put("oldchunks", new OldChunksModule());
        put("block-entity", new BlockEntityModule());
        put("dataless", new DatalessModule());
        put("half-door", new HalfDoorModule());
        put("activated-spawner", new ActivatedSpawnerModule());
    }


    private void put(String name, SearchModule instance) {
        moduleMap.put(name, instance);
    }

    public SearchModule findModule(String moduleName) throws ModuleNotFoundException {
        final SearchModule module = moduleMap.get(moduleName);

        if (module == null) throw new ModuleNotFoundException();
        return module;
    }

    public List<SearchModule> getEnabledModules() {
        return moduleMap.values().stream()
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

                try {
                    var ret = mod.processChunkBlockEntities(blockEntities);
                    instance.getOutputMode().processChunkResult(mod.getName(), ret);
                } catch (Exception e) {
                    instance.logger.error("Failed to process chunk block entities {}: {}", chunk.getX() + "," + chunk.getZ(), e.getMessage());
                }
                return;
            }

            try {
                var ret = module.processChunk(chunk);
                instance.getOutputMode().processChunkResult(module.getName(), ret);
            } catch (Exception e) {
                instance.logger.error("Failed to process chunk {}: {}", chunk.getX() + "," + chunk.getZ(), e.getMessage());
            }
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

                    try {
                        var ret = module.processEntities(entities);
                        instance.getOutputMode().processChunkResult(module.getName(), ret);
                    } catch (Exception e) {
                        instance.logger.error("Failed to process chunk entities {}: {}", chunk.getX() + "," + chunk.getZ(), e.getMessage());
                    }
                });
    }
}
