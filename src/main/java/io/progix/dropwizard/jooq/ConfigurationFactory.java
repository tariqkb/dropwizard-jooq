package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jooq.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    @Context
    HttpServletRequest request;

    private final Configuration configuration;

    public ConfigurationFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration provide() {
        if(request != null) {
            return ConnectionProviderContext.hasBind() ? configuration.derive(ConnectionProviderContext.get()) : configuration;
        } else {
            return null;
        }
    }
}
