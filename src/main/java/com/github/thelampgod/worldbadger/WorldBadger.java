package com.github.thelampgod.worldbadger;

import com.github.thelampgod.worldbadger.commands.CommandManager;
import com.github.thelampgod.worldbadger.commands.InputHandler;
import com.github.thelampgod.worldbadger.database.Database;
import com.github.thelampgod.worldbadger.modules.ModuleManager;
import com.github.thelampgod.worldbadger.output.impl.ConsoleOutput;
import com.github.thelampgod.worldbadger.output.impl.CsvOutput;
import com.github.thelampgod.worldbadger.output.OutputMode;
import com.github.thelampgod.worldbadger.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class WorldBadger {

    public Logger logger = LogManager.getLogger(this.getClass());

    @Setter
    private boolean running = true;

    private CommandManager commandManager;
    private WorldManager worldManager;
    private ModuleManager moduleManager;
    private Database database;
    @Setter
    private OutputMode outputMode;

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
        this.outputMode = new ConsoleOutput();
        this.moduleManager = new ModuleManager(instance);
        this.inputHandler = new InputHandler(instance);
        this.database = new Database(instance, "jdbc:sqlite:./worldbadger.db");
        this.database.applySchema();
    }
}
