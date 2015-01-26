package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.schema.tables.records.BookRecord;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;

import java.sql.SQLException;

/**
 *
 */
public class MockedDataProvider implements MockDataProvider {
    @Override
    public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
        return new MockResult[0];
    }
}
