package com.github.thelampgod;

import com.github.thelampgod.worldbadger.WorldBadger;

public class Main {
    // TODO: start terminal controller if no args or some setting
    // with tab autocomplete and stuff for commands and id names
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            new WorldBadger();
            return;
        }
    }

}