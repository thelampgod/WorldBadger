package com.github.thelampgod.worldbadger.commands;

import com.github.thelampgod.worldbadger.WorldBadger;

public abstract class Command {

    protected final WorldBadger main;

    public Command(WorldBadger main) {
        this.main = main;
    }

    public void exec(String[] args) {
        if (requiredArgs() > 0) {
            if (args == null || args.length < requiredArgs()) {
                throw new IllegalArgumentException("Too few args.");
            }
        }
    }

    public String description() {
        return "";
    }

    public int requiredArgs() {
        return 0;
    }

}
