package com.github.thelampgod.worldbadger.util.blocks;


import lombok.Data;

import java.util.Map;

@Data
public class BlockState {
    private final int x;
    private final int y;
    private final int z;

    private final String id;

    private final Map<String, String> properties;

    public boolean positionMatches(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }
}
