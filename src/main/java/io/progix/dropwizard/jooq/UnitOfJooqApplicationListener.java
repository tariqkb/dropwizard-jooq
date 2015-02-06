package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConfiguration;
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

    private Map<Method, UnitOfJooq> methodMap = new HashMap<Method, UnitOfJooq>();
    private DefaultConfiguration base;
    private DataSource dataSource;

    public UnitOfJooqApplicationListener(DefaultConfiguration base, DataSource dataSource) {
        this.base = base;
        this.dataSource = dataSource;
    }

    private static class UnitOfJooqEventListener implements RequestEventListener {
        private final Map<Method, UnitOfJooq> methodMap;
        private UnitOfJooq unitOfJooq;

        private DefaultConfiguration base;
        private DataSource dataSource;

        public UnitOfJooqEventListener(Map<Method, UnitOfJooq> methodMap, DefaultConfiguration base, DataSource dataSource) {
            this.methodMap = methodMap;
            this.base = base;
            this.dataSource = dataSource;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.unitOfJooq = this.methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());

                if (unitOfJooq != null) {
                    try {
                        ResourceConfigurationContext.bind((DefaultConfiguration) base.derive(dataSource.getConnection()));
                    } catch (SQLException e) {
                        throw new DataAccessException("An error occurred while retrieving a connection.", e);
                    }
                }
            } else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                //commit the connection and close it
                ((DefaultConnectionProvider) ResourceConfigurationContext.get().connectionProvider()).commit();

                try {
                    ((DefaultConnectionProvider) ResourceConfigurationContext.get().connectionProvider()).acquire().close();
                } catch (SQLException e) {
                    throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                } finally {
                    ResourceConfigurationContext.unbind();
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                //rollback the connection and close it
                ((DefaultConnectionProvider) ResourceConfigurationContext.get().connectionProvider()).rollback();

                try {
                    ((DefaultConnectionProvider) ResourceConfigurationContext.get().connectionProvider()).acquire().close();
                } catch (SQLException e) {
                    throw new DataAccessException("An error occurred while attempting to close a connection.", e);
                } finally {
                    ResourceConfigurationContext.unbind();
                }
            }
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
        return new UnitOfJooqEventListener(methodMap, base, dataSource);
    }

    private void registerUnitOfWorkAnnotations(ResourceMethod method) {
        UnitOfJooq annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfJooq.class);

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }

    }
}
