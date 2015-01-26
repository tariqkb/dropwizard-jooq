package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.schema.Tables;
import io.progix.dropwizard.jooq.schema.tables.daos.AuthorDao;
import io.progix.dropwizard.jooq.schema.tables.pojos.Author;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExampleResource {

    private final AuthorDao dao;

    public ExampleResource(AuthorDao dao) {
        this.dao = dao;
    }

    @GET
    @Path("/injectConfig")
    @UnitOfJooq
    public List<Author> get(Configuration config) {
        DSLContext context = DSL.using(config);

        List<Author> authors = context.select().from(Tables.AUTHOR).fetchInto(Author.class);

        return authors;
    }

    @GET
    @Path("/dao")
    @UnitOfJooq
    public List<Author> get() {
        List<Author> authors = dao.findAll();

        return authors;
    }
}
