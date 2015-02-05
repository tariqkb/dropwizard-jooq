package io.progix.dropwizard.jooq;

import org.jooq.impl.DefaultConfiguration;

public class ResourceConfigurationContext {
    private static final ThreadLocal<DefaultConfiguration> configuration_tl = new ThreadLocal<DefaultConfiguration>();

    public static DefaultConfiguration get() {
        return configuration_tl.get();
    }

    public static void bind(DefaultConfiguration configuration) {
        configuration_tl.set(configuration);
    }

    public static void unbind() {
        configuration_tl.remove();
    }
}
