package io.progix.dropwizard.jooq;

import org.jooq.Configuration;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ConfigurationProvider implements ContextResolver<Configuration> {

    @Override
    public Configuration getContext(Class<?> type) {
        if (type == Configuration.class) {
            return ResourceConfigurationContext.get();
        }
        return null;
    }
}
