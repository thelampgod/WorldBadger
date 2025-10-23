package com.github.thelampgod.worldbadger.commands;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commandsMap = new HashMap<>();

    public CommandManager(WorldBadger main) {
        init(main);
    }

    private void init(WorldBadger main) {
        put("add", new AddModuleCommand(main));
        put("run", new RunCommand(main));
        put("world", new SetInputCommand(main));
        put("outputfolder", new SetOutputFolderCommand(main));
        put("commands", new CommandsCommand(main));
        put("outputmode", new SetOutputModeCommand(main));
        put("exit", new ExitCommand(main));
    }

    private void put(String name, Command instance) {
        commandsMap.put(name, instance);
    }

    public Command findCommand(String commandName) throws CommandNotFoundException {
        final Command command = commandsMap.get(commandName);

        if (command == null) throw new CommandNotFoundException();
        return command;
    }

    public String getCommandsFormatted() {
        StringBuilder builder = new StringBuilder("Commands:\n");
        commandsMap.forEach((name, command) -> {
            builder.append("> ").append(name).append(": ").append(command.description()).append("\n");
        });
        return builder.toString();
    }
}
