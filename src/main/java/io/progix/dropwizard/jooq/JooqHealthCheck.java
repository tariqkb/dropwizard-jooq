package io.progix.dropwizard.jooq;

import com.codahale.metrics.health.HealthCheck;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;
import org.jooq.TransactionalRunnable;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

public class JooqHealthCheck extends HealthCheck {

    private final String validationQuery;
    private final DSLContext dslContext;

    public JooqHealthCheck(DSLContext dslContext, String validationQuery) {
        this.validationQuery = validationQuery;
        this.dslContext = dslContext;
    }

    @Override
    protected Result check() throws Exception {
        Result result = dslContext.transactionResult(new TransactionalCallable<Result>() {
            @Override
            public Result run(Configuration configuration) throws Exception {
                dslContext.execute(validationQuery);
                return Result.healthy();
            }
        });
        return result;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public DSLContext getDslContext() {
        return dslContext;
    }
}
