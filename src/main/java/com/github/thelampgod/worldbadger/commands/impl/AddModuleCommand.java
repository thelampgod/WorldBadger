package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.modules.SearchModule;

import java.util.Arrays;

public class AddModuleCommand extends Command {

    public AddModuleCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);
        var opt = main.getModuleManager().findModule(args[0]);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException(args[0] + ": module not found.");
        }
        SearchModule module = opt.get();

        module.options(Arrays.copyOfRange(args,1, args.length));
        module.setToggled(true);

        main.logger.info("{} toggled", module.getName());
    }

    @Override
    public String description() {
        return "Adds a search module to be ran.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}
