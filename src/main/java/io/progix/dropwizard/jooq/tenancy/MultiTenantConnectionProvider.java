package io.progix.dropwizard.jooq.tenancy;

import io.dropwizard.lifecycle.Managed;

import java.sql.Connection;

public interface MultiTenantConnectionProvider extends Managed {

    public Connection acquire(String tenantIdentifier);
}
