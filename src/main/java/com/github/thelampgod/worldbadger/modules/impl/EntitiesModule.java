package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.EntitySearchModule;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.io.snbt.SNBTWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntitiesModule extends EntitySearchModule {
    private static final SNBTWriter nbt = new SNBTWriter();

    public EntitiesModule() {
        super("entities");
    }

    @Override
    public Object processEntities(List<CompoundTag> entities) {
        List<String> foundEntities = new ArrayList<>();
        boolean all = idToOptionsMap.containsKey("all");
        for (CompoundTag tag : entities) {
            String id = tag.getString("id");
            if (!all) {
                if (!idToOptionsMap.containsKey(id)) continue;
            }

            Map<String, String> options = idToOptionsMap.get(all ? "all" : id);
            int minY = options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
            int maxY = options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;

            var pos = tag.getList("Pos");
            double x = pos.getDouble(0);
            double y = pos.getDouble(1);
            double z = pos.getDouble(2);

            if (y >= minY && y <= maxY) {
                foundEntities.add(String.format("%.3f,%.3f,%.3f,%s,%s", x, y, z, id, nbt.toString(tag)));
            }
        }

        return foundEntities.isEmpty() ? null : foundEntities;
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("id", "min", "max");
    }

    @Override
    public boolean requiresId() {
        return false;
    }
}
