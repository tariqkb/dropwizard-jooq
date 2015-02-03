package io.progix.dropwizard.jooq;

import java.sql.Connection;

public class ResourceConnectionContext {
    private static final ThreadLocal<Connection> connection_tl = new ThreadLocal<Connection>();

    public static Connection get() {
        return connection_tl.get();
    }

    public static void bind(Connection connection) {
        connection_tl.set(connection);
    }

    public static void unbind() {
        connection_tl.remove();
    }
}
