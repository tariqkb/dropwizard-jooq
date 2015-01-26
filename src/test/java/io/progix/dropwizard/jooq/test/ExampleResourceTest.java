package io.progix.dropwizard.jooq.test;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.UnitOfJooq;
import io.progix.dropwizard.jooq.schema.Tables;
import io.progix.dropwizard.jooq.schema.tables.daos.AuthorDao;
import io.progix.dropwizard.jooq.schema.tables.pojos.Author;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ExampleResourceTest extends JerseyTest {

    @Path("/authors")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {

        private final AuthorDao dao;

        public ExampleResource(AuthorDao dao) {
            this.dao = dao;
        }

        @GET
        @Path("/injectConfig/{index}")
        @UnitOfJooq
        public Author get(@PathParam("index") int index, Configuration config) {
            DSLContext context = DSL.using(config);

            List<Author> authors = context.select().from(Tables.AUTHOR).fetchInto(Author.class);

            if (authors.get(index) != null) {
                return authors.get(index);
            }

            throw new WebApplicationException(404);
        }

        @GET
        @Path("/dao/{index}")
        @UnitOfJooq
        public Author get(@PathParam("index") int index) {
            List<Author> authors = dao.findAll();

            if (authors.get(index) != null) {
                return authors.get(index);
            }

            throw new WebApplicationException(404);
        }
    }

    @Test
    public void findsExistingDataConfig() throws Exception {
        final Author configAuthor = target("/authors/injectConfig/0").request(MediaType.APPLICATION_JSON).get(Author.class);

        assertThat(configAuthor.getFirstName())
                .isEqualTo("Tariq");

        assertThat(configAuthor.getFirstName())
                .isEqualTo("Bugrara");

        assertThat(configAuthor.getYearOfBirth())
                .isEqualTo(1950);
    }

    @Test
    public void findsExistingDataDao() throws Exception {

        final Author daoAuthor = target("/authors/dao/0").request(MediaType.APPLICATION_JSON).get(Author.class);

        assertThat(daoAuthor.getFirstName())
                .isEqualTo("Tariq");

        assertThat(daoAuthor.getFirstName())
                .isEqualTo("Bugrara");

        assertThat(daoAuthor.getYearOfBirth())
                .isEqualTo(1950);
    }

    @Test
    public void doesNotFindMissingDataConfig() throws Exception {
        try {
            target("/authors/injectConfig/1").request(MediaType.APPLICATION_JSON)
                    .get(Author.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(404);
        }
    }

    @Test
    public void doesNotFindMissingDataDao() throws Exception {
        try {
            target("/authors/dao/1").request(MediaType.APPLICATION_JSON)
                    .get(Author.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(404);
        }
    }

    @Test
    public void createsNewData() throws Exception {
        final Author author = new Author(null, "Alli", "Mars", null, 1951, 0);

        target("/author/injectConfig/1").request().put(Entity.entity(author, MediaType.APPLICATION_JSON));

        final Author alli = target("/people/1")
                .request(MediaType.APPLICATION_JSON)
                .get(Author.class);

        assertThat(alli.getFirstName())
                .isEqualTo("Alli");

        assertThat(alli.getLastName())
                .isEqualTo("Mars");

        assertThat(alli.getYearOfBirth())
                .isEqualTo(1951);

        final Author author2 = new Author(null, "Allison", "Mars", null, 1952, 0);

        target("/author/dao/1").request().put(Entity.entity(author2, MediaType.APPLICATION_JSON));

        final Author alli2 = target("/people/2")
                .request(MediaType.APPLICATION_JSON)
                .get(Author.class);

        assertThat(alli2.getFirstName())
                .isEqualTo("Allison");

        assertThat(alli2.getLastName())
                .isEqualTo("Mars");

        assertThat(alli2.getYearOfBirth())
                .isEqualTo(1952);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator()));
    }

    @Override
    protected Application configure() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        final HibernateBundle<?> bundle = mock(HibernateBundle.class);
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        dbConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.nanoTime()+"?hsqldb.translate_dti_types=false");
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");



        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        config.register(new UnitOfWorkApplicationListener(sessionFactory));
        config.register(new PersonResource(new PersonDAO(sessionFactory)));
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator()));
        return config;
    }
}
