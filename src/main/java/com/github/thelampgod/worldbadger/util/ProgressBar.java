package com.github.thelampgod.worldbadger.util;

import com.github.thelampgod.worldbadger.output.OutputMode;
import com.github.thelampgod.worldbadger.output.impl.ConsoleOutput;

import static com.github.thelampgod.worldbadger.util.Helper.DEL_LINE;

//TODO: track progress chunkwise
public class ProgressBar {
    private final int total;
    private int current;
    private boolean shouldPrint = true;

    private static final int LINE_LENGTH = 50;

    public ProgressBar(int total, OutputMode outputMode) {
        if (outputMode instanceof ConsoleOutput) {
            shouldPrint = false;
        }
        this.total = total;
    }

    public void increment() {
        if (!shouldPrint) return;
        this.current++;
    }

    public void printProgressBar() {
        if (!shouldPrint) return;
        int progress = Math.round((float) current / total * LINE_LENGTH);
        String current = "#".repeat(progress);
        String line = current + ".".repeat(LINE_LENGTH - progress);
        System.out.printf(DEL_LINE);
        System.out.printf("%s (%d/%d regions)", line, this.current, this.total);
    }
}
