package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.ConfigurationProvider;
import io.progix.dropwizard.jooq.JooqBundle;
import io.progix.dropwizard.jooq.JooqHealthCheck;
import io.progix.dropwizard.jooq.UnitOfJooqApplicationListener;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JooqBundleTest {
    private final DataSourceFactory dbConfig = new DataSourceFactory();
    private final io.dropwizard.Configuration configuration = mock(io.dropwizard.Configuration.class);
    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);

    private final JooqBundle<io.dropwizard.Configuration> bundle = new JooqBundle<io.dropwizard.Configuration>() {
        @Override
        protected void configure(Configuration configuration) {
            configuration.set(SQLDialect.HSQLDB);
        }

        @Override
        public DataSourceFactory getDataSourceFactory(io.dropwizard.Configuration configuration) {
            return dbConfig;
        }
    };

    @Before
    public void setUp() {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
    }

    @Test
    public void managesDataSource() throws Exception {
        bundle.run(configuration, environment);

        verify(lifecycleEnvironment).manage(any(ManagedDataSource.class));
    }

    @Test
    public void configuresConfiguration() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getConfiguration().dialect()).isEqualTo(SQLDialect.HSQLDB);
    }

    @Test
    public void registersTransactionListener() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfJooqApplicationListener> captor = ArgumentCaptor.forClass(UnitOfJooqApplicationListener.class);
        verify(jerseyEnvironment).register(captor.capture());
    }

    @Test
    public void registersJooqConfigurationProvider() throws Exception {
        bundle.run(configuration, environment);

        verify(jerseyEnvironment).register(ConfigurationProvider.class);
    }

    @Test
    public void registersJooqHealthCheck() throws Exception {
        dbConfig.setValidationQuery("SELECT 1;");

        bundle.run(configuration, environment);

        final ArgumentCaptor<JooqHealthCheck> captor = ArgumentCaptor.forClass(JooqHealthCheck.class);

        verify(healthChecks).register(eq("jooq"), captor.capture());
        assertThat(captor.getValue().getConfiguration()).isEqualTo(bundle.getConfiguration());
        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT 1;");
    }

}
