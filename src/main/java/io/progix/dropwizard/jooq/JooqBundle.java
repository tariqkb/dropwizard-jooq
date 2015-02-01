package io.progix.dropwizard.jooq;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jooq.Configuration;
import org.jooq.SQLDialect;

public abstract class JooqBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {

    private SQLDialect dialect;
    private Configuration configuration;

    private final JooqConfigurationFactory configurationFactory;

    public JooqBundle(SQLDialect dialect) {
        this.configurationFactory = new JooqConfigurationFactory();
        this.dialect = dialect;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final DataSourceFactory dbConfig = getDataSourceFactory(configuration);

        this.configuration = configurationFactory.build(dialect, environment, dbConfig);

        environment.jersey().register(new UnitOfJooqApplicationListener(configuration));
        environment.healthChecks().register("hibernate", new SessionFactoryHealthCheck(sessionFactory, dbConfig.getValidationQuery()));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
