package com.github.thelampgod.worldbadger.commands;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.impl.AddModuleCommand;
import com.github.thelampgod.worldbadger.commands.impl.RunCommand;
import com.github.thelampgod.worldbadger.commands.impl.SetInputCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandManager {
    private final Map<String, Command> commandsMap = new HashMap<>();

    public CommandManager(WorldBadger main) {
        init(main);
    }

    private void init(WorldBadger main) {
        put("add", new AddModuleCommand(main));
        put("run", new RunCommand(main));
        put("world", new SetInputCommand(main));
    }

    private void put(String name, Command instance) {
        commandsMap.put(name, instance);
    }

    public Command findCommand(String commandName) {
        return commandsMap.get(commandName);
    }
}
