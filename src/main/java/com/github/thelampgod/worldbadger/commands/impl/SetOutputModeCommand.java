package com.github.thelampgod.worldbadger.commands.impl;

import com.github.thelampgod.worldbadger.WorldBadger;
import com.github.thelampgod.worldbadger.commands.Command;
import com.github.thelampgod.worldbadger.output.impl.ConsoleOutput;
import com.github.thelampgod.worldbadger.output.impl.CsvOutput;
import com.github.thelampgod.worldbadger.output.impl.DatabaseOutput;

public class SetOutputModeCommand extends Command {
    public SetOutputModeCommand(WorldBadger main) {
        super(main);
    }

    @Override
    public void exec(String[] args) {
        super.exec(args);

        switch (args[0].toUpperCase()) {
            case "CSV" -> {
                main.setOutputMode(new CsvOutput());
                main.logger.info("Set outputmode to CSV");
            }
            case "DB" -> {
                main.setOutputMode(new DatabaseOutput());
                main.logger.info("Set outputmode to DB");
            }
            default -> {
                main.setOutputMode(new ConsoleOutput());
                main.logger.info("Set outputmode to CONSOLE");
            }
        }
    }

    @Override
    public String description() {
        return "Select between CSV, DB, CONSOLE.";
    }

    @Override
    public int requiredArgs() {
        return 1;
    }
}
