package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;

import java.util.Arrays;

public class ModulesCommand extends Command {
    public ModulesCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);

        StringBuilder builder = new StringBuilder("Modules: \n");

        main.getModuleManager().getModules()
                .forEach(module -> {
                    builder.append("> ")
                            .append(module.getName())
                            .append(": ")
                            .append(module.getDescription())
                            .append(" Options: ")
                            .append(Arrays.toString(module.getValidOptions().toArray()))
                            .append("\n");
                });

        main.logger.info(builder.toString());
    }

    @Override
    public String description() {
        return "Lists all available modules and their usage.";
    }
}
