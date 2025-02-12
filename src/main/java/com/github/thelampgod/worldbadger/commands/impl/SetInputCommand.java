package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.world.World;

import java.io.IOException;

public class SetInputCommand extends Command {

    public SetInputCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        if (args == null || args.length < 1) {
            main.logger.error("usage: setinput <world folder> <dimension>");
            return;
        }
        String folder = args[0];
        int dim = 0;
        if (args.length == 2) {
            try {
                dim = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                main.logger.error("invalid dimension: {}", args[1]);
                main.logger.info("choose between: -1,0,1");
                return;
            }
        }

        try {
            World world = main.getWorldManager().setWorld(folder, dim);

            main.logger.info("Loaded world {} (regions={}, entities={})",
                    world.getWorldRoot(),
                    world.getRegions().size(),
                    world.getEntities().size()
            );
        } catch (IOException e) {
            main.logger.error("Failed to load world: {}", e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Sets the input world and dimension to search in.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}
