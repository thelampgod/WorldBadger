package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetInputCommand extends Command {

    public SetInputCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        if (args == null || args.length < 1) {
            main.logger.error("usage: setinput <world folder> [--dimension=<dim>]");
            main.logger.error("  <world folder>  Path to world folder (use quotes for paths with spaces)");
            main.logger.error("  --dimension     Optional: -1 (nether), 0 (overworld), 1 (end). Default: 0");
            return;
        }

        String folder = null;
        int dim = 0;

        List<String> positionalArgs = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("--dimension=")) {
                try {
                    dim = Integer.parseInt(arg.substring("--dimension=".length()));
                } catch (NumberFormatException e) {
                    main.logger.error("invalid dimension: {}", arg.substring("--dimension=".length()));
                    main.logger.info("choose between: -1, 0, 1");
                    return;
                }
            } else {
                positionalArgs.add(arg);
            }
        }

        if (positionalArgs.isEmpty()) {
            main.logger.error("World folder path is required");
            return;
        }

        folder = String.join(" ", positionalArgs);

        // Validate dimension
        if (dim != -1 && dim != 0 && dim != 1) {
            main.logger.error("invalid dimension: {}", dim);
            main.logger.info("choose between: -1, 0, 1");
            return;
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
