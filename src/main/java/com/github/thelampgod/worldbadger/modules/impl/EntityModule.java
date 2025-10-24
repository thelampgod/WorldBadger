package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.io.snbt.SNBTWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityModule extends EntitySearchModule {
    private static final SNBTWriter nbt = new SNBTWriter();

    public EntityModule() {
        super("entity");
    }

    @Override
    public List<? extends DataClass> processEntities(List<CompoundTag> entities) {
        List<EntityData> foundEntities = new ArrayList<>();
        boolean all = idToOptionsMap.isEmpty() || idToOptionsMap.containsKey("all");

        for (CompoundTag tag : entities) {
            String id = tag.getString("id");
            if (!all) {
                if (!idToOptionsMap.containsKey(id)) continue;
            }

            Map<String, String> options = idToOptionsMap.get(all ? "all" : id);
            int minY = options != null && options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
            int maxY = options != null && options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;
            boolean printNbt = options != null && options.containsKey("nbt");

            var pos = tag.getList("Pos");
            double x = pos.getDouble(0);
            double y = pos.getDouble(1);
            double z = pos.getDouble(2);

            if (y >= minY && y <= maxY) {
                if (printNbt) {
                    foundEntities.add(new EntityData(x, y, z, id, nbt.toString(tag)));
                } else {
                    foundEntities.add(new EntityData(x, y, z, id, null));
                }
            }
        }

        return foundEntities;
    }

    @Data
    private static class EntityData implements DataClass {
        private final double x;
        private final double y;
        private final double z;

        private final String entityId;
        private final String nbt;

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "entityId", "nbt");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, entityId, nbt);
        }
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max", "nbt");
    }

    @Override
    public boolean requiresId() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Find all entities matching, optionally with their nbt data. Usage: id=<id>,option=<option>.";
    }
}
