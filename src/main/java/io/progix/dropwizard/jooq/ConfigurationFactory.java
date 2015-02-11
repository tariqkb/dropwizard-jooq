package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jooq.Configuration;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    private final Configuration configuration;

    public ConfigurationFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration provide() {
        return ConnectionProviderContext.hasBind() ? configuration.derive(ConnectionProviderContext.get()) : configuration;
    }
}
