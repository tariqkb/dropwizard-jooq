package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.tenancy.NoTenantProvider;
import io.progix.dropwizard.jooq.tenancy.TenantProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JooqConfiguration {

    boolean transactional() default true;

    Class<? extends TenantProvider> tenantProvider() default NoTenantProvider.class;
}
