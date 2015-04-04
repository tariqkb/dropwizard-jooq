#Dropwizard jOOQ

This module provides streamlined access to jOOQ in your Dropwizard application.

##Example

```java
private final Configuration config;
private final UserDao userDao;

public UserResource(@JooqConfiguration Configuration config) {
    this.config = config;
    this.userDao = new UserDao(config);
}

@POST
public void createUser(CreateUserRequest request) {
    DSL.using(config).insertInto(USER).set(USER.USERNAME, request.getUsername()).execute();
}

@GET
@Path("{userId}")
public User getUser(@PathParam("userId") int userId) {
    return userDao.fetchOneById(userId);
}
```

After configuring the jOOQ settings, accessing a jOOQ configuration is as easy as injecting it
into the resource either as a method parameter, class field, or constructor parameter.
jOOQ generated DAOs can be created using this configuration or you can use the configuration
to make SQL statements, etc.

By default, all methods are transactional. Any exception thrown during resource execution will cause
the database connection to rollback. Similarly, the connection only commits once execution has completed.

##Getting started

```xml
<dependency>
    <groupId>io.progix.dropwizard</groupId>
    <artifactId>dropwizard-jooq</artifactId>
    <version>0.1.1</version>
</dependency>
```

This module uses the same configuration settings as dropwizard-hibernate and dropwizard-jdbi.

A `DataSourceFactory` is necessary to establish connections to your database. An example configuration
is shown below.

```java
public class ExampleConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
```

Add the `JooqBundle` to your application.

```java
private final JooqBundle<BSTConfiguration> jooq = new JooqBundle<BSTConfiguration>() {
    @Override
    protected void configure(Configuration configuration) {
        configuration.set(SQLDialect.MYSQL);
    }

    @Override
    public DataSourceFactory getDataSourceFactory(BSTConfiguration configuration) {
        return configuration.getDataSourceFactory();
    }
};

@Override
public void initialize(Bootstrap<BSTConfiguration> bootstrap) {
    bootstrap.addBundle(jooq);
}
```

The `configure(Configuration configuration)` of the `JooqBundle` method can be used to apply and extra changes to the
jOOQ `Configuration` that will be used across the application. Any changes made to the `Configuration` parameter
will be applied to this "global" configuration.

An example configuration file is shown below (YAML):

```yaml
database:
  # the name of your JDBC driver
  driverClass: org.postgresql.Driver

  # the username
  user: pg-user

  # the password
  password: iAMs00perSecrEET

  # the JDBC URL
  url: jdbc:postgresql://db.example.com/db-prod

  # any properties specific to your JDBC driver:
  properties:
    charSet: UTF-8

  # the maximum amount of time to wait on an empty pool before throwing an exception
  maxWaitForConnection: 1s

  # the SQL query to run when validating a connection's liveness
  validationQuery: "/* MyService Health Check */ SELECT 1"

  # the minimum number of connections to keep open
  minSize: 8

  # the maximum number of connections to keep open
  maxSize: 32

  # whether or not idle connections should be validated
  checkConnectionWhileIdle: false

  # the amount of time to sleep between runs of the idle connection validation, abandoned cleaner and idle pool resizing
  evictionInterval: 10s

  # the minimum amount of time an connection must sit idle in the pool before it is eligible for eviction
  minIdleTime: 1 minute
```

##Advanced

###Transactions

Resources injected with `@JooqConfiguration Configuration config` are transactional by default.
To remove this behavior, the `@JooqConfiguration` annotation provides a switch for transactional behavior.

```java
@JooqConfiguration(transactional = false) Configuration config
```
Requests that are not transactional depend on the given jOOQ `ConnectionProvider` of the default jOOQ `Configuration`.
In other words, non-transactional requests will use a copy of the default `Configuration`. Note that the `JooqBundle` will
automatically create a `DataSourceConnectionProvider` for the default `Configuration`.

###Tenancy

This module supports separate database and separate schema multi-tenancy out of the box. The following
implementations are required.

####TenantProvider
Implement the `TenantProvider` interface and set the `tenantProvider` field of the `@JooqConfiguration` annotation
to the implemented class. A `TenantProvider`'s sole purpose is to return a `String` identifier for a tenant given
the resource request.

`PathParamTenantProvider` is a simple abstract implementation is provided for simple tenant identification through
a path parameter. To use this provider, extend the class and call the super constructor with the path parameter name
that will be used through out the service.

If more advanced tenant identification is required, the Jersey `ContainerRequest` is provided. See the Jersey
docs for more information.

You must also return the input schema name of generated jOOQ files so that the jOOQ configuration can map
to the specified tenant database/schema.

####TenantConnectionProvider
Implementation of this interface is very flexible and requires you to inject whatever information is required
to provide a JDBC connection for a given tenant identifier. You will most likely want to provide the
`DataSourceFactory` to your implementation and create a map of tenant identifiers and connection pools
for larger applications.

You are also required to implement the methods of Dropwizard's `Managed`. Be sure to close and connections
and stop and `ManagedDataSource`s in the `stop` method.

It is my intention to provide abstract implementations to make implementation of `TenantConnectionProvider`
straightforward once I have a better understanding of common implementations.

##Notes
Please open issues if you find any bugs or if you have any suggestions!