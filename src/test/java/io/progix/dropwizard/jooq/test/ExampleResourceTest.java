package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.*;
import io.progix.dropwizard.jooq.schema.tables.pojos.Author;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static io.progix.dropwizard.jooq.schema.Tables.AUTHOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExampleResourceTest extends JerseyTest {

    @Path("/{tenantId}/authors/{index}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {

        @JooqConfiguration(tenantProvider = TestPathParamTenantProvider.class)
        Configuration config;

        @PathParam("index")
        int index;

        @GET
        public Author get() {
            DSLContext context = DSL.using(config);

            Author author = context.select().from(AUTHOR).where(AUTHOR.ID.equal(index)).fetchOneInto(Author.class);

            if (author != null) {
                return author;
            }

            throw new WebApplicationException(404);
        }

        @PUT
        public Author put() {
            Author a = DSL.using(config).insertInto(AUTHOR).set(AUTHOR.ID, index).set(AUTHOR.NAME, "Alli").returning()
                    .fetchOne().into(Author.class);

            return a;
        }
    }

    @Test
    public void findsExistingData() throws Exception {
        Author configAuthor = target("/BUGRARA/authors/1").request(MediaType.APPLICATION_JSON).get(Author.class);

        assertThat(configAuthor.getName()).isEqualTo("Narmeen");

        configAuthor = target("/PUBLIC/authors/1").request(MediaType.APPLICATION_JSON).get(Author.class);

        assertThat(configAuthor.getName()).isEqualTo("Tariq");
    }

    @Test
    public void doesNotFindMissingData() throws Exception {
        try {
            target("/BUGRARA/authors/2").request(MediaType.APPLICATION_JSON).get(Author.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Test
    public void createsNewData() throws Exception {
        final Author author = new Author(null, "Alli");

        Author alli = target("/BUGRARA/authors/2").request()
                .put(Entity.entity(author, MediaType.APPLICATION_JSON), Author.class);

        assertThat(alli.getName()).isEqualTo("Alli");

    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper()));
    }

    @Override
    protected Application configure() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        String url = "jdbc:hsqldb:mem:dwtest" + System.nanoTime();

        Map<String, String> props = new HashMap<String, String>();
        props.put("username", "sa");
        props.put("password", "");
        props.put("url", url);

        try {
            HSQLDBInit.initPublic(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dbConfig.setUrl(props.get("url"));
        dbConfig.setUser(props.get("user"));
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());

        DataSource dataSource = dbConfig.build(metricRegistry, "jooq");
        config.register(JooqTransactionalApplicationListener.class);

        Configuration configuration = new DefaultConfiguration().set(SQLDialect.HSQLDB);
        configuration.set(new DataSourceConnectionProvider(dataSource));

        config.register(new ConfigurationFactoryProvider.Binder(configuration, dataSource,
                new TestTenantConnectionProvider(dbConfig, metricRegistry, url)));
        config.register(ExampleResource.class);
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper()));
        return config;
    }

}
