package io.progix.dropwizard.jooq;

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

    @Inject
    protected ConfigurationFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, final ServiceLocator injector,
            final ConfigurationFactoryInfo configInfo) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.configuration = configInfo.configuration;
        this.dataSource = configInfo.dataSource;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        return new ConfigurationFactory(configuration, dataSource, parameter.getAnnotation(JooqConfiguration.class));
    }

    @Singleton
    public static class ConfigurationInjectionResolver extends ParamInjectionResolver<JooqConfiguration> {

        public ConfigurationInjectionResolver() {
            super(ConfigurationFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {

        private final DataSource dataSource;
        private final Configuration configuration;

        public Binder(Configuration configuration, DataSource dataSource) {
            this.configuration = configuration;
            this.dataSource = dataSource;
        }

        @Override
        protected void configure() {
            bind(new ConfigurationFactoryInfo(configuration, dataSource)).to(ConfigurationFactoryInfo.class);
            bind(ConfigurationFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(ConfigurationInjectionResolver.class).to(new TypeLiteral<InjectionResolver<JooqConfiguration>>() {
            }).in(Singleton.class);
        }
    }

    public static class ConfigurationFactoryInfo {

        public ConfigurationFactoryInfo(Configuration configuration, DataSource dataSource) {
            this.configuration = configuration;
            this.dataSource = dataSource;
        }

        public Configuration configuration;
        public DataSource dataSource;
    }
}
