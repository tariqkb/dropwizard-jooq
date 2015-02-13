package io.progix.dropwizard.jooq.tenancy;

import org.glassfish.jersey.server.ContainerRequest;

public interface TenantProvider {
    String getTenantIdentifier(ContainerRequest request);

    String inputSchema();
}
