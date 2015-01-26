package io.progix.dropwizard.jooq;

import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalConnectionProvider implements ConnectionProvider {

    private DataSource dataSource;

    private Connection connection;

    public TransactionalConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection acquire() throws DataAccessException {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Error getting connection from data source ", e);
        }
        return this.connection;
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        if (this.connection != connection) {
            throw new DataAccessException("Attempting to release a connection not acquired by this provider");
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Error closing connection " + connection, e);
        }
    }

    public final void commit() throws DataAccessException {
        try {
            connection.commit();
        } catch (Exception e) {
            throw new DataAccessException("Cannot commit transaction", e);
        }
    }

    public final void rollback() throws DataAccessException {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new DataAccessException("Cannot rollback transaction", e);
        }
    }
}
