package com.glassdoor.planout4j.config;

/**
 * Sets config properties for tests to be able to access test namespaces.
 */
public class Planout4jTestConfigHelper {

    /**
     * @param shipper if true, set to test for shipper/compiler (compiled points to temp),
     *                otherwise set to test for repository (compiled points to resources)
     */
    public static void setSystemProperties(final boolean shipper) {
        final String defaultPath = "src/test/resources/namespaces";
        System.setProperty("planout4j.backend.compiledConfDir",
                shipper ? System.getProperty("java.io.tmpdir") : defaultPath);
        System.setProperty("planout4j.backend.sourceConfDir", defaultPath);
    }

    private Planout4jTestConfigHelper() {}

}
