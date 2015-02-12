package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    private final Configuration configuration;

    public ConfigurationFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration provide() {
//        return ConnectionProviderContext.hasBind() ? configuration.derive(ConnectionProviderContext.get()) : configuration;

        ConnectionProvider provider = (ConnectionProvider) getContainerRequest().getProperty("connProvider");
        if(provider != null) {
            return configuration.derive(provider);
        } else {
            return configuration;
        }
    }
}
