package io.progix.dropwizard.jooq.test;

import io.progix.dropwizard.jooq.ConfigurationFactory;
import io.progix.dropwizard.jooq.JooqTransactionalApplicationListener;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.impl.DefaultConnectionProvider;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

public class JooqTransactionalApplicationListenerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final DefaultConnectionProvider connectionProvider = new DefaultConnectionProvider(connection);
    private final JooqTransactionalApplicationListener listener = new JooqTransactionalApplicationListener(

    );

    private final MockResource mockResource = new MockResource();

    private final ApplicationEvent appEvent = mock(ApplicationEvent.class);
    private final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    private final RequestEvent requestStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestFinishEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodExceptionEvent = mock(RequestEvent.class);
    private final ContainerRequest containerRequest = mock(ContainerRequest.class);

    @Before
    public void setUp() throws SQLException, NoSuchMethodException {
        when(dataSource.getConnection()).thenReturn(connection);

        when(appEvent.getType()).thenReturn(ApplicationEvent.Type.INITIALIZATION_APP_FINISHED);
        when(requestFinishEvent.getType()).thenReturn(RequestEvent.Type.FINISHED);
        when(requestMethodExceptionEvent.getType()).thenReturn(RequestEvent.Type.ON_EXCEPTION);
        when(requestFinishEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodExceptionEvent.getUriInfo()).thenReturn(uriInfo);

        when(containerRequest.getProperty(ConfigurationFactory.CONNECTION_PROVIDER_PROPERTY)).thenReturn(connectionProvider);

        when(requestFinishEvent.getContainerRequest()).thenReturn(containerRequest);
        when(requestMethodExceptionEvent.getContainerRequest()).thenReturn(containerRequest);

        requestFinishEvent.getContainerRequest().getProperty(ConfigurationFactory.CONNECTION_PROVIDER_PROPERTY);
    }

    @Test
    public void generatesRequestEventListener() throws NoSuchMethodException {
        prepareAppEvent("methodWithDefaultAnnotation");
        listener.onEvent(appEvent);

        JooqTransactionalApplicationListener.JooqTransactionalRequestListener requestEventListener = (JooqTransactionalApplicationListener.JooqTransactionalRequestListener) listener
                .onRequest(requestStartEvent);

        assertThat(requestEventListener).isNotNull();
    }

    @Test
    public void commitsAndClosesOnSuccessfulFinish() throws SQLException, NoSuchMethodException {
        prepareAppEvent("methodWithDefaultAnnotation");
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);

        when(requestFinishEvent.isSuccess()).thenReturn(true);

        requestListener.onEvent(requestFinishEvent);

        verify(connection).commit();
        verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    public void doesNotCommitOrCloseOnUnsuccessfulFinish() throws SQLException, NoSuchMethodException {
        prepareAppEvent("methodWithDefaultAnnotation");
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);

        when(requestFinishEvent.isSuccess()).thenReturn(false);

        requestListener.onEvent(requestFinishEvent);

        verify(connection, never()).commit();
        verify(connection, never()).close();
    }

    @Test
    public void rollbackAndCloseOnException() throws SQLException, NoSuchMethodException {
        prepareAppEvent("methodWithDefaultAnnotation");
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);

        requestListener.onEvent(requestMethodExceptionEvent);

        verify(connection).rollback();
        verify(connection).close();
        verify(connection, never()).commit();

    }

    private void prepareAppEvent(String resourceMethodName) throws NoSuchMethodException {
        final Resource.Builder builder = Resource.builder();

        final Method method = mockResource.getClass().getMethod(resourceMethodName);
        final ResourceMethod resourceMethod = builder.addMethod().handlingMethod(method).handledBy(mockResource, method).build();
        final Resource resource = builder.build();
        final ResourceModel model = new ResourceModel.Builder(false).addResource(resource).build();

        when(appEvent.getResourceModel()).thenReturn(model);
        when(uriInfo.getMatchedResourceMethod()).thenReturn(resourceMethod);
    }

    public static class MockResource {

        public void methodWithDefaultAnnotation() {

        }

    }

}
