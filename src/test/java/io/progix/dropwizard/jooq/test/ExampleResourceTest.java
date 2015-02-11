package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.ConfigurationFactoryProvider;
import io.progix.dropwizard.jooq.HSQLDBInit;
import io.progix.dropwizard.jooq.UnitOfJooq;
import io.progix.dropwizard.jooq.UnitOfJooqApplicationListener;
import io.progix.dropwizard.jooq.schema.tables.pojos.Author;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static io.progix.dropwizard.jooq.schema.Tables.AUTHOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExampleResourceTest extends JerseyTest {

    @Path("/authors")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {

        @GET
        @Path("/{index}")
        @UnitOfJooq
        public Author get(@PathParam("index") int index, @Context Configuration config) {
            DSLContext context = DSL.using(config);

            Author author = context.select().from(AUTHOR).where(AUTHOR.ID.equal(index)).fetchOneInto(Author.class);

            if (author != null) {
                return author;
            }

            throw new WebApplicationException(404);
        }

        @PUT
        @Path("/{index}")
        @UnitOfJooq
        public Author put(@PathParam("index") int index, @Context Configuration config) {
            Author a = DSL.using(config).insertInto(AUTHOR).set(AUTHOR.ID, index).set(AUTHOR.NAME, "Alli").returning().fetchOne().into(Author.class);

            return a;
        }
    }

    @Test
    public void findsExistingData() throws Exception {
        final Author configAuthor = target("/authors/1").request(MediaType.APPLICATION_JSON).get(Author.class);

        assertThat(configAuthor.getName()).isEqualTo("Tariq");
    }

    @Test
    public void doesNotFindMissingData() throws Exception {
        try {
            target("/authors/2").request(MediaType.APPLICATION_JSON).get(Author.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Test
    public void createsNewData() throws Exception {
        final Author author = new Author(null, "Alli");

        Author alli = target("/authors/2").request().put(Entity.entity(author, MediaType.APPLICATION_JSON), Author.class);

        assertThat(alli.getName()).isEqualTo("Alli");

    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator()));
    }

    @Override
    protected Application configure() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        Map<String, String> props = new HashMap<String, String>();
        props.put("username", "sa");
        props.put("password", "");
        props.put("url", "jdbc:hsqldb:mem:dwtest" + System.nanoTime());

        try {
            HSQLDBInit.init(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dbConfig.setUrl(props.get("url"));
        dbConfig.setUser(props.get("user"));
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());

        config.register(new UnitOfJooqApplicationListener(dbConfig.build(metricRegistry, "jooq")));

        config.register(new ConfigurationFactoryProvider.Binder(new DefaultConfiguration().set(SQLDialect.HSQLDB)));
        config.register(new ExampleResource());
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator()));
        return config;
    }
}
