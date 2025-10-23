package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public class SignModule extends BlockEntitySearchModule {
    public SignModule() {
        super("sign");
    }

    @Override
    public List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities) {
        List<SignData> signs = new ArrayList<>();

        blockEntities.stream()
                .filter(tag -> tag.getString("id").equals("minecraft:sign"))
//                .filter(this::isInRange)  //TODO: coordinate settings
                .forEach(sign -> {
                    signs.add(SignData.fromModel(sign));
                });

        return signs;
    }

    @Data
    private static class SignData implements DataClass {
        private final int x;
        private final int y;
        private final int z;

        private final String[] frontText; //TODO: back_text?
        private final String color;
        private final boolean glowing;

        public static SignData fromModel(CompoundTag sign) {
            var front_text = sign.getCompound("front_text");
            ListTag messages = front_text.getList("messages");
            String[] temp = new String[4];
            for (int i = 0; i < 4; ++i) {
                temp[i] = messages.getString(i);
            }


            return new SignData(sign.getInt("x"),
                    sign.getInt("y"),
                    sign.getInt("z"),
                    temp,
                    front_text.getString("color"),
                    front_text.getBoolean("has_glowing_text")
            );
        }

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "frontText", "color", "glowing");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, frontText, color, glowing);
        }
    }
}
