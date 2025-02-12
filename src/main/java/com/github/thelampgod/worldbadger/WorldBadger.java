package com.github.thelampgod.worldbadger;

import com.github.thelampgod.worldbadger.commands.CommandManager;
import com.github.thelampgod.worldbadger.commands.InputHandler;
import com.github.thelampgod.worldbadger.modules.ModuleManager;
import com.github.thelampgod.worldbadger.world.WorldManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Getter
public class WorldBadger {

    public Logger logger = LogManager.getLogger(this.getClass());

    private CommandManager commandManager;
    private WorldManager worldManager;
    private ModuleManager moduleManager;

    private InputHandler inputHandler;

    public WorldBadger() {
        logger.info("Starting WorldBadger 1.0");
        init(this);
        logger.info("Welcome! Type \"commands\" for commands");

        inputHandler.listenForInput();
    }

    private void init(WorldBadger instance) {
        this.commandManager = new CommandManager(instance);
        this.worldManager = new WorldManager(instance);
        this.moduleManager = new ModuleManager();
        this.inputHandler = new InputHandler(instance);
    }
}
