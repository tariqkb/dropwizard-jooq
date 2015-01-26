package io.progix.dropwizard.jooq;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;

public class DWJooqConfigurationFactory {

    private final ManagedDataSource dataSource;
    private final TransactionalConnectionProvider connectionProvider;

    public DWJooqConfigurationFactory(Environment environment, DataSourceFactory dbConfig) {
        this.dataSource = dbConfig.build(environment.metrics(), "jooq");
        this.connectionProvider = new TransactionalConnectionProvider(dataSource);
    }

    public DWJooqConfiguration build(SQLDialect dialect) {
        return new DWJooqConfiguration(dialect, connectionProvider);
    }

    public static class DWJooqConfiguration extends DefaultConfiguration {

        public DWJooqConfiguration(SQLDialect dialect, ConnectionProvider connectionProvider) {
            set(dialect);
            set(connectionProvider);
        }
    }
}
