package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.modules.ModuleNotFoundException;
import com.github.thelampgod.worldbadger.modules.SearchModule;

import java.util.Arrays;

public class ToggleModuleCommand extends Command {

    public ToggleModuleCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);
        try {
            SearchModule module = main.getModuleManager().findModule(args[0].trim().toLowerCase());

            module.toggle();
            module.options(Arrays.copyOfRange(args, 1, args.length));

            if (module.isToggled()) {
                main.logger.info("Added {} to module list.", module.getName());
            } else {
                main.logger.info("Removed {} from module list.", module.getName());
            }
        } catch (ModuleNotFoundException e) {
            main.logger.error("{}: module not found.", args[0]);
        }
    }

    @Override
    public String description() {
        return "Toggles a search module.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}