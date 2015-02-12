package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.health.HealthCheck;
import io.progix.dropwizard.jooq.JooqHealthCheck;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TransactionalCallable;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import sun.security.krb5.Config;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JooqHealthCheckTest {
    private final MockDataProvider provider = new MockDataProvider() {
        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            MockResult[] mock = new MockResult[1];

            // The execute context contains SQL string(s), bind values, and other meta-data
            String sql = ctx.sql();

            // Exceptions are propagated through the JDBC and jOOQ APIs
            if (sql.equals("SELECT 1")) {
                mock[0] = new MockResult(1, DSL.using(SQLDialect.HSQLDB).newResult());
            }

            return mock;
        }
    };
    private final MockConnection connection = new MockConnection(provider);

    private final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
    private final DSLContext dslContext = mock(DSLContext.class);
    private JooqHealthCheck healthCheck = new JooqHealthCheck(dslContext, "SELECT 1");

    @Before
    public void setUp() {
        when(connectionProvider.acquire()).thenReturn(connection);

    }

    @Test
    public void hasADSLContext() throws Exception {
        assertThat(healthCheck.getDslContext()).isEqualTo(dslContext);
    }

    @Test
    public void hasAValidationQuery() throws Exception {
        assertThat(healthCheck.getValidationQuery()).isEqualTo("SELECT 1");
    }

    @Test
    public void isHealthyIfNoExceptionIsThrown() throws Exception {
//        assertThat(healthCheck.execute()).isEqualTo(HealthCheck.Result.healthy());
//
//        final InOrder inOrder = inOrder(connectionProvider);
//        inOrder.verify(connectionProvider).acquire();
//        inOrder.verify(connectionProvider).commit();
//        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    @Ignore
    public void isUnhealthyIfAnExceptionIsThrown() throws Exception {
//        healthCheck = new JooqHealthCheck(dslContext, "SELECT 2");
//
//        assertThat(healthCheck.execute().isHealthy()).isFalse();
//
//        final InOrder inOrder = inOrder(connectionProvider);
//        inOrder.verify(connectionProvider).acquire();
//        inOrder.verify(connectionProvider).rollback();
//        assertThat(connection.isClosed()).isTrue();
    }
}
