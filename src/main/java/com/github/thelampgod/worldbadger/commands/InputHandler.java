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
        while (scanner.hasNext()) {
            System.out.print("> ");
            String[] split = scanner.nextLine().split(" ");

            String commandName = split[0];
            String[] commandArgs = null;
            if (split.length > 1) {
                commandArgs = Arrays.copyOfRange(split, 1, split.length);
            }

            var command = main.getCommandManager().findCommand(commandName);

            if (command == null) {
                System.err.println(commandName + ": command not found.");
                return;
            }

            try {
                command.exec(commandArgs);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

    }
}
