package io.progix.dropwizard.jooq;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConnectionProvider;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
public class JooqTransactionalApplicationListener implements ApplicationEventListener {

    public static class JooqTransactionalRequestListener implements RequestEventListener {

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.FINISHED) {
                DefaultConnectionProvider connectionProvider = (DefaultConnectionProvider) event.getContainerRequest()
                        .getProperty(ConfigurationFactory.CONNECTION_PROVIDER_PROPERTY);
                if (connectionProvider != null && event.isSuccess()) {
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

    }

    @Override
    public void onEvent(ApplicationEvent event) {

    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        return new JooqTransactionalRequestListener();
    }

}