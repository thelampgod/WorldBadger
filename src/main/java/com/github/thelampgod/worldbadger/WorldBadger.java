package com.github.thelampgod.worldbadger;

import com.github.thelampgod.worldbadger.commands.CommandManager;
import com.github.thelampgod.worldbadger.commands.InputHandler;
import com.github.thelampgod.worldbadger.database.Database;
import com.github.thelampgod.worldbadger.modules.ModuleManager;
import com.github.thelampgod.worldbadger.world.WorldManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


@Getter
public class WorldBadger {

    public Logger logger = LogManager.getLogger(this.getClass());

    private CommandManager commandManager;
    private WorldManager worldManager;
    private ModuleManager moduleManager;
    private Database database;

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
        this.database = new Database(instance, "jdbc:sqlite:./worldbadger.db");
        this.database.applySchema();
    }
}
