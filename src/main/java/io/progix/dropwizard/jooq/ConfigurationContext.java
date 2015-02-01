package io.progix.dropwizard.jooq;

import org.jooq.Configuration;

public class ConfigurationContext {
    private static final ThreadLocal<Configuration> configuration_tl = new ThreadLocal<Configuration>();

    public static Configuration get() {
        return configuration_tl.get();
    }

    public static void bind(Configuration configuration) {
        configuration_tl.set(configuration);
    }

    public static void unbind() {
        configuration_tl.remove();
    }
}
