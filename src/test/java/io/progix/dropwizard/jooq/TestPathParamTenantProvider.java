package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.tenancy.PathParamTenantProvider;

public class TestPathParamTenantProvider extends PathParamTenantProvider {

    public TestPathParamTenantProvider() {
        super("tenantId");
    }

    @Override
    public String inputSchema() {
        return "PUBLIC";
    }
}
