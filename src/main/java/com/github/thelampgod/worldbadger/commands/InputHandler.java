package com.github.thelampgod.worldbadger.commands;

import com.github.thelampgod.worldbadger.WorldBadger;

import java.util.Arrays;
import java.util.Scanner;

public class InputHandler {
    private final WorldBadger main;

    private final Scanner scanner;

    public InputHandler(WorldBadger main) {
        this.main = main;
        this.scanner = new Scanner(System.in);
    }

    public void listenForInput() {
        System.out.print("> ");
        while (main.isRunning()) {
            String[] split = scanner.nextLine().split(" ");

            String commandName = split[0];
            String[] commandArgs = null;
            if (split.length > 1) {
                commandArgs = Arrays.copyOfRange(split, 1, split.length);
            }

            try {
                var command = main.getCommandManager().findCommand(commandName);
                command.exec(commandArgs);

            } catch (CommandNotFoundException e) {
                main.logger.error("{}: command not found.", commandName);
            } catch (Exception e) {
                main.logger.error(e.getMessage());
            } finally {
                System.out.print("> ");
            }
        }

    }
}
