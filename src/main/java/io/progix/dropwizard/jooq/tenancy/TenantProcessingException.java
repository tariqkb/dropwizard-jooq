package io.progix.dropwizard.jooq.tenancy;

public class TenantProcessingException extends RuntimeException {

    public TenantProcessingException() {
        super();
    }

    public TenantProcessingException(String message) {
        super(message);
    }

    public TenantProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantProcessingException(Throwable cause) {
        super(cause);
    }

    protected TenantProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
