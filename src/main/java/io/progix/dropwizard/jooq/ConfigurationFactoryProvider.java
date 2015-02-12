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
import org.jooq.SQLDialect;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;

@Singleton
public class ConfigurationFactoryProvider extends AbstractValueFactoryProvider {

    private final Configuration configuration;

    @Inject
    protected ConfigurationFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, final ServiceLocator injector,
            final Configuration configuration) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.configuration = configuration;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        return new ConfigurationFactory(configuration);
    }

    @Singleton
    public static class ConfigurationInjectionResolver extends ParamInjectionResolver<JooqConfiguration> {

        public ConfigurationInjectionResolver() {
            super(ConfigurationFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {

        private Configuration configuration;

        public Binder(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        protected void configure() {
            bind(configuration).to(Configuration.class);
            bind(ConfigurationFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(ConfigurationInjectionResolver.class).to(new TypeLiteral<InjectionResolver<JooqConfiguration>>() {
            }).in(Singleton.class);
        }
    }
}
