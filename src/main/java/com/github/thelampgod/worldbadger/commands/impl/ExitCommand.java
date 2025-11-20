package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;

public class ExitCommand extends Command {
    public ExitCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);

        main.logger.info("Bye!");
        main.setRunning(false);
        main.getWorldManager().stopSearch();
    }

    @Override
    public String description() {
        return "Exit the program.";
    }
}
