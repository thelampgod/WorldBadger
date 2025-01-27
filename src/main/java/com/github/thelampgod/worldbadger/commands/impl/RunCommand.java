package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.modules.SearchModule;

public class RunCommand extends Command {
    public RunCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);
        var world = main.getWorldManager().getWorld();
        var enabledModules = main.getModuleManager().getEnabledModules();

        System.out.printf("Starting search of world %s (regions=%d, entities=%d) with %d modules:%n",
                world.getWorldRoot(),
                world.getRegions().size(),
                world.getEntities().size(),
                enabledModules.size()
        );

        for (int i = 0; i < enabledModules.size(); ++i) {
            String moduleName = enabledModules.get(i).getName();

            if (i != enabledModules.size() - 1) {
                System.out.printf("%s, ", moduleName);
                continue;
            }
            System.out.printf(moduleName);
        }

        main.getWorldManager().startSearch();
        System.out.println("Done!");
    }

    @Override
    public String description() {
        return "Starts the search";
    }


}
