package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConnectionProvider;

import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Provider
public class UnitOfJooqApplicationListener implements ApplicationEventListener {

    private final Map<Method, UnitOfJooq> methodMap = new HashMap<>();

    public static class UnitOfJooqEventListener implements RequestEventListener {

        private final Map<Method, UnitOfJooq> methodMap;

        public UnitOfJooqEventListener(Map<Method, UnitOfJooq> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                DefaultConnectionProvider connectionProvider = (DefaultConnectionProvider) event.getContainerRequest()
                        .getProperty(ConfigurationFactory.CONNECTION_PROVIDER_PROPERTY);
                if (connectionProvider != null) {
                    //commit the connection and close it
                    connectionProvider.commit();

                    try {
                        connectionProvider.acquire().close();
                    } catch (SQLException e) {
                        throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                    }
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                DefaultConnectionProvider connectionProvider = (DefaultConnectionProvider) event.getContainerRequest()
                        .getProperty(ConfigurationFactory.CONNECTION_PROVIDER_PROPERTY);

                if (connectionProvider != null) {
                    //rollback the connection and close it
                    connectionProvider.rollback();

                    try {
                        connectionProvider.acquire().close();
                    } catch (SQLException e) {
                        throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                    }
                }
            }
        }

        public Map<Method, UnitOfJooq> getMethodMap() {
            return methodMap;
        }

    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            for (Resource resource : event.getResourceModel().getResources()) {
                for (ResourceMethod method : resource.getAllMethods()) {
                    registerUnitOfWorkAnnotations(method);
                }

                for (Resource childResource : resource.getChildResources()) {
                    for (ResourceMethod method : childResource.getAllMethods()) {
                        registerUnitOfWorkAnnotations(method);
                    }
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        return new UnitOfJooqEventListener(methodMap);
    }

    private void registerUnitOfWorkAnnotations(ResourceMethod method) {
        UnitOfJooq annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfJooq.class);

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }

    }

    public Map<Method, UnitOfJooq> getMethodMap() {
        return methodMap;
    }
}