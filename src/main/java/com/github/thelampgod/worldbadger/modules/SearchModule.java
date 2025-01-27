package com.github.thelampgod.worldbadger.modules;

import lombok.Getter;
import lombok.Setter;
import net.querz.mca.Chunk;

@Getter
@Setter
public abstract class SearchModule {

    private final String name;
    private boolean toggled = false;

    public SearchModule(String name) {
        this.name = name;
    }

    public abstract Object processChunk(Chunk chunk);


    public void toggle() {
        this.toggled = !toggled;
    }

    public void options(String[] options) {
    }
}
