package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;

public class RunCommand extends Command {
    public RunCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);
        var world = main.getWorldManager().getWorld();
        var enabledModules = main.getModuleManager().getEnabledModules();

        main.logger.info("Starting search of world {} (regions={}, entities={}) with {} modules:",
                world.getWorldRoot(),
                world.getRegions().size(),
                world.getEntities().size(),
                enabledModules.size()
        );

        for (int i = 0; i < enabledModules.size(); ++i) {
            String moduleName = enabledModules.get(i).getName();

            if (i != enabledModules.size() - 1) {
                main.logger.info("{}, ", moduleName);
                continue;
            }
            main.logger.info(moduleName);
        }

        try {
            main.getWorldManager().startSearch();
        } catch (Exception e) {
            main.logger.error("Couldn't run search: {}", e.getMessage());
        }
        main.logger.info("Done!");
    }

    @Override
    public String description() {
        return "Starts the search";
    }


}
