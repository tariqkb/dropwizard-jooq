package io.progix.dropwizard.jooq.tenancy;

import org.glassfish.jersey.server.ContainerRequest;

public class NoTenantProvider implements TenantProvider {
    @Override
    public String getTenantIdentifier(ContainerRequest request) {
        return null;
    }

    @Override
    public String inputSchema() {
        return null;
    }
}
