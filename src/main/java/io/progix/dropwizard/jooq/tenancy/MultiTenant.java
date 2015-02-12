package io.progix.dropwizard.jooq.tenancy;

public @interface MultiTenant {

    Class<TenantProvider> provider();
}
