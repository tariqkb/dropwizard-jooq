package io.progix.dropwizard.jooq;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;

public class JooqConfigurationFactory {

    private final ManagedDataSource dataSource;
    private final TransactionalConnectionProvider connectionProvider;

    public JooqConfigurationFactory(Environment environment, DataSourceFactory dbConfig) {
        this.dataSource = dbConfig.build(environment.metrics(), "jooq");
        this.connectionProvider = new TransactionalConnectionProvider(dataSource);
    }

    public Configuration build(SQLDialect dialect) {
        Configuration configuration = new DefaultConfiguration();

        configuration.set(dialect);
        configuration.set(connectionProvider.acquire());

        return configuration;
    }

}
