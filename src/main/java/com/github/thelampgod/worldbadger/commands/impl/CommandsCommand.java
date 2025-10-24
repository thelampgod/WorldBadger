package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;

public class CommandsCommand extends Command {
    public CommandsCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);

        main.logger.info(main.getCommandManager().getCommandsFormatted());
    }

    @Override
    public String description() {
        return "Prints command usage.";
    }
}
