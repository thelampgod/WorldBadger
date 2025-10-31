package com.github.thelampgod.worldbadger.util;

import net.querz.nbt.io.snbt.SNBTWriter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

public class Helper {

    public static final SNBTWriter SNBT_WRITER = new SNBTWriter();
    public static final String DEL_LINE = "\33[2K\r" ;

    public static Path getResourceAsPath(URL resource, String resourcePath) throws IOException, URISyntaxException {
        Path directory;
        URI uri = resource.toURI();
        if (uri.getScheme().equals("jar")) {
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            directory = fs.getPath(resourcePath);
        } else {
            directory = Path.of(resource.toURI());
        }
        return directory;
    }

}
