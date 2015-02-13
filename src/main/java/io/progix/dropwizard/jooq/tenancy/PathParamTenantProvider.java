package io.progix.dropwizard.jooq.tenancy;

import org.glassfish.jersey.server.ContainerRequest;

public abstract class PathParamTenantProvider implements TenantProvider {

    private final String pathParamIdentifier;

    public PathParamTenantProvider(String pathParamIdentifier) {
        this.pathParamIdentifier = pathParamIdentifier;
    }

    @Override
    public String getTenantIdentifier(ContainerRequest request) {
        return request.getUriInfo().getPathParameters().getFirst(pathParamIdentifier);
    }
}
