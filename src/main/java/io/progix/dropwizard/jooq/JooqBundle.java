package io.progix.dropwizard.jooq;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jooq.Configuration;
import org.jooq.impl.DefaultConfiguration;

public abstract class JooqBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {

    private DefaultConfiguration configuration;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final DataSourceFactory dbConfig = getDataSourceFactory(configuration);
        ManagedDataSource dataSource = dbConfig.build(environment.metrics(), "jooq");

        this.configuration = new DefaultConfiguration();
        configure(this.configuration);

        environment.jersey().register(new UnitOfJooqApplicationListener(this.configuration, dataSource));
        environment.jersey().register(ConfigurationProvider.class);

        Configuration healthCheckConfiguration = this.configuration.derive(dataSource.getConnection());
        environment.healthChecks().register("jooq", new JooqHealthCheck(healthCheckConfiguration, dbConfig.getValidationQuery()));
    }

    public Configuration getConfiguration() {
        return configuration.derive();
    }

    protected abstract void configure(Configuration configuration);

}
