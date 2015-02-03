package io.progix.dropwizard.jooq;

import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ThreadConnectionProvider implements ConnectionProvider {

    private final ThreadLocal<Connection> connection_tl;
    private DataSource dataSource;

    public ThreadConnectionProvider(DataSource dataSource) {
        this.connection_tl = new ThreadLocal<Connection>();
        this.dataSource = dataSource;
    }

    @Override
    public Connection acquire() throws DataAccessException {
        Connection connection = connection_tl.get();
        if(connection == null) {
            try {
                connection = dataSource.getConnection();
            } catch (SQLException e) {
                throw new DataAccessException("An error occurred while acquiring a connection.", e);
            }
            connection_tl.set(connection);
        }
        return connection;
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        if(connection == connection_tl.get()) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException("An error occurred while releasing a connection to it's pool.", e);
            }
        } else {
            throw new IllegalArgumentException("The connection attempting to be released is not the same connection associated with the thread.");
        }
    }
}
