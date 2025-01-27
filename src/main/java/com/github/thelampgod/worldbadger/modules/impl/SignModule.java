package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.querz.mca.Chunk;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignModule extends BlockEntitySearchModule {
    public SignModule() {
        super("sign");
    }

    @Override
    public Object processChunkBlockEntities(List<CompoundTag> blockEntities) {
        List<SignData> signs = new ArrayList<>();

        blockEntities.stream()
                .filter(tag -> tag.getString("id").equals("minecraft:sign"))
//                .filter(this::isInRange)  //TODO: coordinate settings
                .forEach(sign -> {
                    signs.add(SignData.fromModel(sign));
                });

        return signs.isEmpty() ? null : signs;
    }

    @Override
    public Object processChunk(Chunk chunk) {
        return null; // no op
    }


    @RequiredArgsConstructor
    @Data
    public static class SignData {
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
        public String toString() {
            return String.format("%d,%d,%d,%s,%s,%s", x,y,z,Arrays.toString(frontText), color, glowing);
        }
    }
}
