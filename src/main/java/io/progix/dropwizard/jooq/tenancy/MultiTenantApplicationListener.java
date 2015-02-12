package io.progix.dropwizard.jooq.tenancy;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MultiTenantApplicationListener implements ApplicationEventListener {

    private final Map<Method, MultiTenant> methodMap = new HashMap<>();

    public static class MultiTenantEventListener implements RequestEventListener {

        @Override
        public void onEvent(RequestEvent event) {

        }
    }
    @Override
    public void onEvent(ApplicationEvent event) {

    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null;
    }
}
