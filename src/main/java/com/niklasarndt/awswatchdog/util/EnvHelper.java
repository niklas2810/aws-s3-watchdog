package com.niklasarndt.awswatchdog.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Optional;

/**
 * Created by Niklas on 2020/12/25.
 */
public class EnvHelper {

    public static final boolean DEBUG = System.getenv("AWS_WATCHDOG_DEBUG") != null;
    private static final Dotenv dotenv = DEBUG ? Dotenv.configure().ignoreIfMissing().load() : null;

    static {
        if (DEBUG)
            System.out.println("-".repeat(10) + " WARNING: THIS APP IS RUNNING IN DEBUG MODE "
                    + "-".repeat(10));
    }

    public static Optional<String> get(String key) {
        return Optional.ofNullable(DEBUG ? dotenv.get(key) : System.getenv(key));
    }

    public static String get(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    public static String require(String key) {
        return get(key).orElseThrow(() ->
                new IllegalStateException("EnvHelper value for key \""
                        + key + "\" has not been set! This is a required value."));
    }

    public static boolean has(String key) {
        return get(key).isPresent();
    }

    public static int requireInt(String key) {
        try {
            return Integer.parseInt(require(key));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("EnvHelper value for key \""
                    + key + "\" is present, but not an integer! This is a required number.", e);
        }
    }
}
