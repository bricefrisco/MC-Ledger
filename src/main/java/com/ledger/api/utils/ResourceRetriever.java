package com.ledger.api.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Contains utility methods to retrieve file names and contents at runtime.
 * All static resources should be placed in the src/main/resources directory
 */
public class ResourceRetriever {

    /**
     * Retrieves a list of file paths from the resources directory
     * @param basePath - the base path to check in resources
     * @return a list of file paths
     */
    public static List<String> getResourcePaths(String basePath) throws IOException, URISyntaxException {
        List<String> result = new ArrayList<>();

        URL url = ResourceRetriever.class.getClassLoader().getResource(basePath);
        if (url == null) {
            throw new FileNotFoundException("Cannot find resource at '" + basePath + "'");
        }

        URI uri = url.toURI();
        FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        Path path = fileSystem.getPath(basePath);

        Stream<Path> walk = Files.walk(path);
        for (Iterator<Path> it = walk.iterator(); it.hasNext();) {
            Path p = it.next();
            if (!Files.isDirectory(p)) {
                result.add(p.toString());
            }
        }

        walk.close();
        fileSystem.close();

        return result;
    }

    /**
     * Retrieve the contents of a file
     * @param path - the path of the file in the resources directory
     * @return the file contents
     */
    public static String getFileContents(String path) throws IOException {
        try (InputStream stream = ResourceRetriever.class.getClassLoader().getResourceAsStream(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static byte[] getFileBytes(String path) throws IOException {
        try (InputStream stream = ResourceRetriever.class.getClassLoader().getResourceAsStream(path)) {
            return stream.readAllBytes();
        }
    }
}
