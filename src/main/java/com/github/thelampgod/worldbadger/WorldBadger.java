package com.github.thelampgod.worldbadger;

import com.github.thelampgod.worldbadger.commands.CommandManager;
import com.github.thelampgod.worldbadger.commands.InputHandler;
import com.github.thelampgod.worldbadger.modules.ModuleManager;
import com.github.thelampgod.worldbadger.world.WorldManager;
import lombok.Getter;

@Getter
public class WorldBadger {
    private CommandManager commandManager;
    private WorldManager worldManager;
    private ModuleManager moduleManager;

    private InputHandler inputHandler;

    public WorldBadger() {
        System.out.println("Starting WorldBadger 1.0");
        init(this);
        System.out.println("Welcome! Type \"commands\" for commands");

        inputHandler.listenForInput();
    }

    private void init(WorldBadger instance) {
        this.commandManager = new CommandManager(instance);
        this.worldManager = new WorldManager(instance);
        this.moduleManager = new ModuleManager();
        this.inputHandler = new InputHandler(instance);
    }
}
