package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.health.HealthCheck;
import io.progix.dropwizard.jooq.JooqHealthCheck;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

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
            } else {
                throw new DataAccessException("Incorrect validation query");
            }

            return mock;
        }
    };
    private final MockConnection connection = new MockConnection(provider);
    private final Configuration configuration = new DefaultConfiguration();

    private DSLContext dslContext;
    private JooqHealthCheck healthCheck;

    @Before
    public void setUp() {
        configuration.set(new DefaultConnectionProvider(connection));
        dslContext = DSL.using(configuration);
        healthCheck = new JooqHealthCheck(dslContext, "SELECT 1");
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
        assertThat(healthCheck.execute()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void isUnhealthyIfAnExceptionIsThrown() throws Exception {
        healthCheck = new JooqHealthCheck(dslContext, "SELECT 2");

        assertThat(healthCheck.execute().isHealthy()).isFalse();
    }
}
