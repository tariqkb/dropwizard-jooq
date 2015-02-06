package io.progix.dropwizard.jooq.test;

import io.dropwizard.hibernate.UnitOfWorkApplicationListener;
import io.progix.dropwizard.jooq.ResourceConfigurationContext;
import io.progix.dropwizard.jooq.UnitOfJooq;
import io.progix.dropwizard.jooq.UnitOfJooqApplicationListener;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UnitOfJooqApplicationListenerTest {

    private final DefaultConfiguration configuration = new DefaultConfiguration();
    private final DefaultConnectionProvider connectionProvider = new DefaultConnectionProvider();
    private final UnitOfJooqApplicationListener listener = new UnitOfJooqApplicationListener(configuration);

    private final ApplicationEvent appEvent = mock(ApplicationEvent.class);
    private final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);

    private final RequestEvent requestStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodFinishEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodExceptionEvent = mock(RequestEvent.class);

    @Before
    public void setUp() throws SQLException, NoSuchMethodException {
        when(appEvent.getType()).thenReturn(ApplicationEvent.Type.INITIALIZATION_APP_FINISHED);
        when(requestMethodStartEvent.getType()).thenReturn(RequestEvent.Type.RESOURCE_METHOD_START);
        when(requestMethodFinishEvent.getType()).thenReturn(RequestEvent.Type.RESOURCE_METHOD_FINISHED);
        when(requestMethodExceptionEvent.getType()).thenReturn(RequestEvent.Type.ON_EXCEPTION);
        when(requestMethodStartEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodFinishEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodExceptionEvent.getUriInfo()).thenReturn(uriInfo);

        prepareAppEvent("methodWithDefaultAnnotation");
    }

    @Test
    public void opensConnectionAndDerivesConfiguration() throws SQLException {
        execute();

        final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);

        verify(connectionProvider).acquire();
        verify(configuration).derive(captor.capture());
        assertThat(ResourceConfigurationContext.hasBind()).isFalse();
    }

    @Test
    public void opensAndCommits() throws SQLException {
        execute();

        final InOrder inOrder = inOrder(connectionProvider);

        inOrder.verify(connectionProvider).acquire();
        inOrder.verify(connectionProvider).commit();
        inOrder.verify(connectionProvider.acquire()).close();
        assertThat(ResourceConfigurationContext.hasBind()).isFalse();
    }

    @Test
    public void opensAndRollsback() throws SQLException {
        executeWithException();

        final InOrder inOrder = inOrder(connectionProvider);

        inOrder.verify(connectionProvider).acquire();
        inOrder.verify(connectionProvider).commit();
        inOrder.verify(connectionProvider.acquire()).close();
        assertThat(ResourceConfigurationContext.hasBind()).isFalse();
    }

    @Test
    public void bindsAndUnbindsTheConfigurationToTheContext() throws Exception {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                assertThat(ResourceConfigurationContext.get()).isEqualTo(configuration);
                return null;
            }
        }).when(connectionProvider).commit();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                assertThat(ResourceConfigurationContext.get()).isEqualTo(configuration);
                return null;
            }
        }).when(connectionProvider).rollback();

        execute();

        assertThat(ResourceConfigurationContext.hasBind()).isFalse();
    }

    private void prepareAppEvent(String resourceMethodName) throws NoSuchMethodException {
        final Resource.Builder builder = Resource.builder();
        final MockResource mockResource = new MockResource();
        final Method method = mockResource.getClass().getMethod(resourceMethodName);
        final ResourceMethod resourceMethod = builder.addMethod()
                .handlingMethod(method)
                .handledBy(mockResource, method).build();
        final Resource resource = builder.build();
        final ResourceModel model = new ResourceModel.Builder(false).addResource(resource).build();

        when(appEvent.getResourceModel()).thenReturn(model);
        when(uriInfo.getMatchedResourceMethod()).thenReturn(resourceMethod);
    }

    private void execute() {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);
    }

    private void executeWithException() {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodExceptionEvent);
    }

    public static class MockResource {

        @UnitOfJooq
        public void methodWithDefaultAnnotation() {
        }
    }
}
