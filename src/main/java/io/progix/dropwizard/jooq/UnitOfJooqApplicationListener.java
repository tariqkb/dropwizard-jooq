package io.progix.dropwizard.jooq;

import io.dropwizard.Configuration;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.context.internal.ManagedSessionContext;

import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Map;

@Provider
public class UnitOfJooqApplicationListener implements ApplicationEventListener {

    private Configuration configuration;

    public UnitOfJooqApplicationListener(Configuration configuration) {
        this.configuration = configuration;
    }

    private static class UnitOfJooqEventListener implements RequestEventListener {
        private final Map<Method, UnitOfJooq> methodMap;
        private UnitOfJooq unitOfJooq;

        public UnitOfJooqEventListener(Map<Method, UnitOfJooq> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.unitOfJooq = this.methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());

                if (unitOfJooq != null) {
                    // get connection for this request
                    ResourceConnectionContext.bind(dataSource.getConnection());
                }
            } else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                //commit the connection and close it
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                //rollback the connection
            }
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
