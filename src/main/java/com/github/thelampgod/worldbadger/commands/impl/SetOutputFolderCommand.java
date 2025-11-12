package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;

public class SetOutputFolderCommand extends Command {

    public SetOutputFolderCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        if (args == null || args.length < 1) {
            main.logger.error("usage: setoutput <folder>");
            return;
        }
        String folder = args[0];
        try {
            main.getWorldManager().setOutputFolder(folder);

            main.logger.info("Set output folder to {}", folder);
        } catch (Exception e) {
            main.logger.error(e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Sets the output folder to save results in.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}