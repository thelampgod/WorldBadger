package com.github.thelampgod.worldbadger.output;

import java.nio.file.Path;
import java.util.List;

public interface OutputMode {

    // Create resultfolder, result files, or database tables for database impl
    void initialize(Path outputFolder) throws Exception;

    void processChunkResult(String moduleName, List<? extends DataClass> results);

    void close();
}
