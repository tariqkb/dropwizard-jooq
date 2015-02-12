package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConnectionProvider;

import javax.sql.DataSource;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Provider
public class UnitOfJooqApplicationListener implements ApplicationEventListener {

    private final Map<Method, UnitOfJooq> methodMap = new HashMap<>();
    private final DataSource dataSource;

    public UnitOfJooqApplicationListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static class UnitOfJooqEventListener implements RequestEventListener {

        private final Map<Method, UnitOfJooq> methodMap;

        private final DefaultConnectionProvider connectionProvider;

        public UnitOfJooqEventListener(Map<Method, UnitOfJooq> methodMap, DefaultConnectionProvider connectionProvider) {
            this.methodMap = methodMap;
            this.connectionProvider = connectionProvider;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                UnitOfJooq unitOfJooq = this.methodMap.get(event.getUriInfo().getMatchedResourceMethod().getInvocable().getDefinitionMethod());

                if (unitOfJooq != null) {
                    ConnectionProviderContext.bind(connectionProvider);
                }
            } else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                if (ConnectionProviderContext.hasBind()) {
                    //commit the connection and close it
                    ConnectionProviderContext.get().commit();

                    try {
                        ConnectionProviderContext.get().acquire().close();
                    } catch (SQLException e) {
                        throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                    } finally {
                        ConnectionProviderContext.unbind();
                    }
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                if (ConnectionProviderContext.hasBind()) {
                    //rollback the connection and close it
                    ConnectionProviderContext.get().rollback();

                    try {
                        ConnectionProviderContext.get().acquire().close();
                    } catch (SQLException e) {
                        throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                    } finally {
                        ConnectionProviderContext.unbind();
                    }
                }
            }
        }

        public Map<Method, UnitOfJooq> getMethodMap() {
            return methodMap;
        }

        public DefaultConnectionProvider getConnectionProvider() {
            return connectionProvider;
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
        try {
            Connection connection = dataSource.getConnection();
            return new UnitOfJooqEventListener(methodMap, new DefaultConnectionProvider(connection));
        } catch (SQLException e) {
            throw new DataAccessException("An error occurred while opening a connection", e);
        }
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