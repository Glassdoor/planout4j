package com.glassdoor.planout4j.config;

import com.google.common.io.Files;
import com.typesafe.config.Config;

import java.io.InputStream;
import java.util.*;

/**
 * A {@link Planout4jConfigBackend} which loads namespace content found on the classpath.
 *
 * @author rschatz
 */
public class Planout4jConfigClasspathBackend implements Planout4jConfigBackend {

    /**
     * Returns the name of the directory on the classpath which contains the namespace files.
     * @return String
     */
    protected String getPlanoutSubdirectory() {
        return "planout";
    }

    /**
     * {@link Planout4jConfigClasspathBackend} does not need to be configured.
     *
     * @param config any {@link Config}
     */
    @Override
    public void configure(Config config) { }

    /**
     * Loads and returns a mapping of namespace names to json based on files in the {@code planout} resource directory.
     *
     * @return a map of namespace name to content in the corresponding persistence layer
     */
    @Override
    public Map<String, String> loadAll() {
        final Map<String, String> name2Content = new HashMap<>();

        for (String jsonFile : getJsonResourceFiles()) {
            final String namespace = Files.getNameWithoutExtension(jsonFile);
            final String fileName = String.format(
                "%s%s%s", getPlanoutSubdirectory(), System.getProperty("file.separator"), jsonFile
            );
            final String json = readJson(fileName);
            name2Content.put(namespace, json);
        }
        return name2Content;
    }

    private String readJson(final String jsonFileName) {
        return readInputStream(
            Planout4jConfigClasspathBackend.class.getClassLoader().getResourceAsStream(jsonFileName)
        );
    }

    private Iterable<String> getJsonResourceFiles() {
        final List<String> files = new ArrayList<>();

        final InputStream in = Planout4jConfigClasspathBackend.class.getClassLoader().getResourceAsStream(
            String.format("%s/", getPlanoutSubdirectory())
        );

        try (final Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                final String fileName = scanner.nextLine();
                if (fileName.endsWith(".json")) {
                    files.add(fileName);
                }
            }
        }
        return files;
    }

    private String readInputStream(InputStream is) {
        try (final Scanner scanner = new Scanner(is).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * {@link Planout4jConfigClasspathBackend} does not persist anything.
     *
     * @param namespace2Content namespace2Content to be stored
     */
    @Override
    public void persist(Map<String, String> namespace2Content) {
        throw new UnsupportedOperationException(String.format("%s does not support persisting.", this.getClass().getSimpleName()));
    }

    @Override
    public String persistenceLayer() {
        return "CLASSPATH";
    }

    @Override
    public String persistenceDestination() {
        return this.getClass().getName();
    }
}
