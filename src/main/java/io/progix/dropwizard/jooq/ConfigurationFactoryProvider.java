package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.tenancy.TenantConnectionProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.jooq.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class ConfigurationFactoryProvider extends AbstractValueFactoryProvider {

    private final Configuration configuration;
    private final DataSource dataSource;
    private final TenantConnectionProvider multiTenantConnectionProvider;

    @Inject
    protected ConfigurationFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
            final ServiceLocator injector, final ConfigurationFactoryInfo configInfo) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.configuration = configInfo.configuration;
        this.dataSource = configInfo.dataSource;
        this.multiTenantConnectionProvider = configInfo.multiTenantConnectionProvider;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        Class<?> classType = parameter.getRawType();

        if (classType == null || (!classType.equals(Configuration.class))) {
            return null;
        }

        return new ConfigurationFactory(configuration, dataSource, multiTenantConnectionProvider,
                parameter.getAnnotation(JooqConfiguration.class));
    }

    @Singleton
    public static class ConfigurationInjectionResolver extends ParamInjectionResolver<JooqConfiguration> {

        public ConfigurationInjectionResolver() {
            super(ConfigurationFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {

        private final Configuration configuration;
        private final DataSource dataSource;
        private final TenantConnectionProvider multiTenantConnectionProvider;

        public Binder(Configuration configuration, DataSource dataSource,
                TenantConnectionProvider multiTenantConnectionProvider) {
            this.configuration = configuration;
            this.dataSource = dataSource;
            this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        }

        public Binder(Configuration configuration, DataSource dataSource) {
            this.configuration = configuration;
            this.dataSource = dataSource;
            this.multiTenantConnectionProvider = null;
        }

        @Override
        protected void configure() {
            bind(new ConfigurationFactoryInfo(configuration, dataSource, multiTenantConnectionProvider))
                    .to(ConfigurationFactoryInfo.class);
            bind(ConfigurationFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(ConfigurationInjectionResolver.class).to(new TypeLiteral<InjectionResolver<JooqConfiguration>>() {
            }).in(Singleton.class);
        }
    }

    public static class ConfigurationFactoryInfo {

        public Configuration configuration;
        public DataSource dataSource;
        public TenantConnectionProvider multiTenantConnectionProvider;

        public ConfigurationFactoryInfo(Configuration configuration, DataSource dataSource,
                TenantConnectionProvider multiTenantConnectionProvider) {
            this.configuration = configuration;
            this.dataSource = dataSource;
            this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        }
    }
}
