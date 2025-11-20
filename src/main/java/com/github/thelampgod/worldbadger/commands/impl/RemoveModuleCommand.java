package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.modules.ModuleNotFoundException;
import com.github.thelampgod.worldbadger.modules.SearchModule;

public class RemoveModuleCommand extends Command {

    public RemoveModuleCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);
        try {
            SearchModule module = main.getModuleManager().findModule(args[0].trim().toLowerCase());

            module.getIdToOptionsMap().clear();
            module.setToggled(false);

            main.logger.info("Removed {} from module list.", module.getName());
        } catch (ModuleNotFoundException e) {
            main.logger.error("{}: module not found.", args[0]);
        }
    }

    @Override
    public String description() {
        return "Removes a module to be ran.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}
