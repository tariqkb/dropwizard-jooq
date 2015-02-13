package io.progix.dropwizard.jooq.test;

import io.progix.dropwizard.jooq.ConnectionProviderContext;
import io.progix.dropwizard.jooq.UnitOfJooq;
import io.progix.dropwizard.jooq.UnitOfJooqApplicationListener;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UnitOfJooqApplicationListenerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UnitOfJooqApplicationListener listener = new UnitOfJooqApplicationListener(

    );

    private final MockResource mockResource = new MockResource();

    private final ApplicationEvent appEvent = mock(ApplicationEvent.class);
    private final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    private final RequestEvent requestStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodFinishEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodExceptionEvent = mock(RequestEvent.class);

    @Before
    public void setUp() throws SQLException, NoSuchMethodException {
        when(dataSource.getConnection()).thenReturn(connection);

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
    public void registersAllUnitOfJooqAnnotatedMethods() throws NoSuchMethodException {
        prepareAppEventAll();
        listener.onEvent(appEvent);

        Map<Method, UnitOfJooq> methodMap = expectedMap();

        assertThat(listener.getMethodMap()).isEqualTo(methodMap);
    }

    @Test
    public void generatesRequestEventListener() throws NoSuchMethodException {
        prepareAppEventAll();
        listener.onEvent(appEvent);

        UnitOfJooqApplicationListener.UnitOfJooqEventListener requestEventListener = (UnitOfJooqApplicationListener.UnitOfJooqEventListener) listener
                .onRequest(requestStartEvent);

        Map<Method, UnitOfJooq> expectedMethodMap = expectedMap();

//        assertThat(requestEventListener.getConnectionProvider().acquire()).isEqualTo(connection);
        assertThat(requestEventListener.getMethodMap()).isEqualTo(expectedMethodMap);
    }

    @Test
    public void bindsConnProviderCommitsClosesAndUnbinds() throws SQLException {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        assertThat(ConnectionProviderContext.hasBind()).isFalse();

        requestListener.onEvent(requestMethodStartEvent);
        assertThat(ConnectionProviderContext.hasBind()).isTrue();

        requestListener.onEvent(requestMethodFinishEvent);
        verify(connection).commit();
        verify(connection).close();

        assertThat(ConnectionProviderContext.hasBind()).isFalse();
    }

    @Test
    public void bindsConnProviderRollbackClosesAndUnbinds() throws SQLException {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        assertThat(ConnectionProviderContext.hasBind()).isFalse();

        requestListener.onEvent(requestMethodStartEvent);
        assertThat(ConnectionProviderContext.hasBind()).isTrue();

        requestListener.onEvent(requestMethodExceptionEvent);
        verify(connection).rollback();
        verify(connection).close();

        assertThat(ConnectionProviderContext.hasBind()).isFalse();
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

    private void prepareAppEventAll() throws NoSuchMethodException {
        final Resource.Builder builder = Resource.builder();

        final ResourceModel.Builder modelBuilder = new ResourceModel.Builder(false);

        final Method[] methods = mockResource.getClass().getMethods();
        for (Method method : methods) {
            final ResourceMethod resourceMethod = builder.addMethod("get" + method.getName()).handlingMethod(method).handledBy(mockResource, method).build();
            final Resource resource = builder.build();
            modelBuilder.addResource(resource);

            when(uriInfo.getMatchedResourceMethod()).thenReturn(resourceMethod);
        }

        final ResourceModel model = modelBuilder.build();

        when(appEvent.getResourceModel()).thenReturn(model);
    }

    public static class MockResource {

        @UnitOfJooq
        public void methodWithDefaultAnnotation() {
        }

        public void methodWithNoAnnotation() {
        }
    }

    private Map<Method, UnitOfJooq> expectedMap() throws NoSuchMethodException {
        Map<Method, UnitOfJooq> methodMap = new HashMap<Method, UnitOfJooq>();
        methodMap.put(mockResource.getClass().getMethod("methodWithDefaultAnnotation"),
                mockResource.getClass().getMethod("methodWithDefaultAnnotation").getAnnotation(UnitOfJooq.class));

        return methodMap;
    }
}
