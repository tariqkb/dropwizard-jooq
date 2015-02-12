package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.*;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, String> props = new HashMap<String, String>();
        props.put("username", "sa");
        props.put("password", "");
        props.put("url", "jdbc:hsqldb:mem:dwtest" + System.nanoTime());

        try {
            HSQLDBInit.init(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dbConfig.setUrl(props.get("url"));
        dbConfig.setUser(props.get("user"));
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
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
        verify(jerseyEnvironment, times(2)).register(captor.capture());
        final ArgumentCaptor<ConfigurationFactoryProvider> configProviderCaptor = ArgumentCaptor.forClass(ConfigurationFactoryProvider.class);
        verify(jerseyEnvironment, times(2)).register(configProviderCaptor.capture());
    }

    @Test
    public void registersJooqHealthCheck() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<JooqHealthCheck> captor = ArgumentCaptor.forClass(JooqHealthCheck.class);

        verify(healthChecks).register(eq("jooq"), captor.capture());
        assertThat(captor.getValue().getDslContext().configuration().dialect()).isEqualTo(bundle.getConfiguration().dialect());
        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    }

}
