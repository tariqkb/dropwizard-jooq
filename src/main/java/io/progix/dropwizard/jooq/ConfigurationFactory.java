package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConnectionProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    public static final String CONNECTION_PROVIDER_PROPERTY = "jooqConnectionProvider";

    private final Configuration configuration;
    private final DataSource dataSource;
    private final JooqConfiguration annotation;

    public ConfigurationFactory(Configuration configuration, DataSource dataSource, JooqConfiguration annotation) {
        this.configuration = configuration;
        this.dataSource = dataSource;
        this.annotation = annotation;
    }

    @Override
    public Configuration provide() {
        if (annotation.transactional()) {
            try {
                ConnectionProvider provider = new DefaultConnectionProvider(dataSource.getConnection());
                getContainerRequest().setProperty(CONNECTION_PROVIDER_PROPERTY, provider);
                return configuration.derive(provider);
            } catch (SQLException e) {
                throw new DataAccessException("There was a problem opening a connection", e);
            }
        } else {
            return configuration.derive();
        }
    }
}
