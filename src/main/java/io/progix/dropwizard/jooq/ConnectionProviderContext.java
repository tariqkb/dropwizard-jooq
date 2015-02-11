package io.progix.dropwizard.jooq;

import org.jooq.impl.DefaultConnectionProvider;

public class ConnectionProviderContext {
    private static final ThreadLocal<DefaultConnectionProvider> CONNECTION_PROVIDER_THREAD_LOCAL = new ThreadLocal<DefaultConnectionProvider>();

    public static DefaultConnectionProvider get() {
        return CONNECTION_PROVIDER_THREAD_LOCAL.get();
    }

    public static void bind(DefaultConnectionProvider connectionProvider) {
        CONNECTION_PROVIDER_THREAD_LOCAL.set(connectionProvider);
    }

    public static void unbind() {
        CONNECTION_PROVIDER_THREAD_LOCAL.remove();
    }

    public static boolean hasBind() { return CONNECTION_PROVIDER_THREAD_LOCAL.get() != null; }
}
