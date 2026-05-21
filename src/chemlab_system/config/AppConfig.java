package chemlab_system.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Central configuration loader for the ChemLab System desktop application.
 *
 * Priority order for each property:
 * 1. config.properties file located in the same folder as the running JAR
 * (dist/)
 * 2. JVM system property (e.g. -Dsupabase.password=...)
 * 3. null (callers should fail fast with a clear message)
 *
 * Setup: create a file named "config.properties" in the dist/ folder
 * (next to ChemLab_System.jar) with the following content:
 *
 * supabase.host=aws-0-ap-northeast-2.pooler.supabase.com
 * supabase.port=6543
 * supabase.dbname=postgres
 * supabase.user=postgres.YOUR_PROJECT_ID
 * supabase.password=YOUR_DB_PASSWORD
 *
 * This file should NEVER be committed to Git.
 */
public class AppConfig {

    private static final Properties props = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Search for config.properties in multiple locations (first found wins):
        // 1. Same directory as the running JAR (production: dist/)
        // 2. Parent directory of the JAR (NetBeans dev: dist/run{random}/ → dist/)
        // 3. Current working directory (IDE run from source)
        File[] candidates;
        try {
            File codeSource = new File(
                    AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File jarDir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            candidates = new File[] {
                    new File(jarDir, "config.properties"),
                    new File(jarDir.getParentFile(), "config.properties"),
                    new File(System.getProperty("user.dir"), "config.properties")
            };
        } catch (Exception e) {
            candidates = new File[] {
                    new File(System.getProperty("user.dir"), "config.properties")
            };
        }

        for (File configFile : candidates) {
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    System.out.println("AppConfig: loaded config from " + configFile.getAbsolutePath());
                    return; // Stop at first successful load
                } catch (Exception e) {
                    System.out.println(
                            "AppConfig: failed to read " + configFile.getAbsolutePath() + " — " + e.getMessage());
                }
            }
        }

        // None of the candidate locations had a config.properties
        System.out.println("AppConfig: config.properties not found in any of the search locations.");
        System.out.println("  Searched: " + java.util.Arrays.toString(candidates));
        System.out.println("  Please create 'config.properties' in the dist/ folder.");
        System.out.println("  See 'config.properties.template' for the required format.");
        System.out.println("  Falling back to JVM system properties (-Dsupabase.* flags).");
    }

    /**
     * Returns the value for the given key, or null if not found.
     */
    public static String get(String key) {
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return System.getProperty(key);
    }

    /**
     * Returns the value for the given key, or defaultValue if not found.
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns the value for the given key, or throws IllegalStateException
     * with a helpful message if the key is missing. Use for required config.
     */
    public static String getRequired(String key) {
        String value = get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Required configuration key '" + key + "' is missing.\n" +
                            "Please create 'config.properties' in the same folder as the application JAR (dist/).\n" +
                            "See 'config.properties.template' for the required format.");
        }
        return value.trim();
    }
}
