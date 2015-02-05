package io.progix.dropwizard.jooq;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class JooqConfigurationFactory {

    private final ManagedDataSource dataSource;

    public JooqConfigurationFactory(Environment environment, DataSourceFactory dbConfig) {
        this.dataSource = dbConfig.build(environment.metrics(), "jooq");
    }

    public DefaultConfiguration build(SQLDialect dialect) {
        DefaultConfiguration configuration = new DefaultConfiguration();

        configuration.set(dialect);

        return configuration;
    }

}
