/**
 * This class is generated by jOOQ
 */
package io.progix.dropwizard.jooq.schema.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.1"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuthorDao extends org.jooq.impl.DAOImpl<io.progix.dropwizard.jooq.schema.tables.records.AuthorRecord, io.progix.dropwizard.jooq.schema.tables.pojos.Author, java.lang.Integer> {

	/**
	 * Create a new AuthorDao without any configuration
	 */
	public AuthorDao() {
		super(io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR, io.progix.dropwizard.jooq.schema.tables.pojos.Author.class);
	}

	/**
	 * Create a new AuthorDao with an attached configuration
	 */
	public AuthorDao(org.jooq.Configuration configuration) {
		super(io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR, io.progix.dropwizard.jooq.schema.tables.pojos.Author.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(io.progix.dropwizard.jooq.schema.tables.pojos.Author object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>ID IN (values)</code>
	 */
	public java.util.List<io.progix.dropwizard.jooq.schema.tables.pojos.Author> fetchById(java.lang.Integer... values) {
		return fetch(io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>ID = value</code>
	 */
	public io.progix.dropwizard.jooq.schema.tables.pojos.Author fetchOneById(java.lang.Integer value) {
		return fetchOne(io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR.ID, value);
	}

	/**
	 * Fetch records that have <code>NAME IN (values)</code>
	 */
	public java.util.List<io.progix.dropwizard.jooq.schema.tables.pojos.Author> fetchByName(java.lang.String... values) {
		return fetch(io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR.NAME, values);
	}
}
