package com.github.thelampgod.worldbadger.modules.impl;

import com.github.thelampgod.worldbadger.modules.BlockEntitySearchModule;
import com.github.thelampgod.worldbadger.output.DataClass;
import lombok.Data;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.List;

public class SignModule extends BlockEntitySearchModule {
    public SignModule() {
        super("sign");
    }

    @Override
    public List<? extends DataClass> processChunkBlockEntities(List<CompoundTag> blockEntities) {
        return blockEntities.stream()
                .filter(tag -> tag.getString("id").equals("minecraft:sign") ||
                                            tag.getString("id").equals("minecraft:hanging_sign"))
                .filter(this::isInRange)
                .map(SignData::fromModel)
                .toList();
    }

    private boolean isInRange(CompoundTag sign) {
        var options = this.idToOptionsMap.get("all");
        int minY = options != null && options.containsKey("min") ? Integer.parseInt(options.get("min")) : Integer.MIN_VALUE;
        int maxY = options != null && options.containsKey("max") ? Integer.parseInt(options.get("max")) : Integer.MAX_VALUE;

        final int signY = sign.getInt("y");

        return signY >= minY && signY <= maxY;
    }

    @Data
    private static class SignData implements DataClass {
        private final int x;
        private final int y;
        private final int z;
        private final String id; //hanging_sign or sign

        private final Message frontText;
        private final Message backText;

        public static SignData fromModel(CompoundTag sign) {
            Message frontText = getMessages(sign.getCompound("front_text"));
            Message backText = getMessages(sign.getCompound("back_text"));


            return new SignData(
                    sign.getInt("x"),
                    sign.getInt("y"),
                    sign.getInt("z"),
                    sign.getString("id"),
                    frontText,
                    backText
            );
        }

        private static Message getMessages(CompoundTag tag) {
            ListTag messages = tag.getList("messages");
            String[] lines = new String[4];
            for (int i = 0; i < 4; ++i) {
                lines[i] = messages.getString(i);
            }

            return new Message(lines, tag.getString("color"), tag.getBoolean("has_glowing_text"));
        }

        @Override
        public List<String> getFieldNames() {
            return List.of("x", "y", "z", "frontText", "backText");
        }

        @Override
        public List<Object> getFieldValues() {
            return List.of(x, y, z, frontText, backText);
        }
    }

    @Data
    private static class Message {
        private final String[] lines;
        private final String color;
        private final boolean glowing;
    }

    @Override
    public String getDescription() {
        return "Find all signs in the world with their data.";
    }

    @Override
    public List<String> getValidOptions() {
        return List.of("min", "max");
    }

    @Override
    public boolean requiresId() {
        return false;
    }
}
