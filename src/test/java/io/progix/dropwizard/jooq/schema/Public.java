/**
 * This class is generated by jOOQ
 */
package io.progix.dropwizard.jooq.schema;

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
public class Public extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -1620351691;

	/**
	 * The reference instance of <code>public</code>
	 */
	public static final Public PUBLIC = new Public();

	/**
	 * No further instances allowed
	 */
	private Public() {
		super("public");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			io.progix.dropwizard.jooq.schema.Sequences.AUTHOR_ID_SEQ,
			io.progix.dropwizard.jooq.schema.Sequences.BOOK_ID_SEQ,
			io.progix.dropwizard.jooq.schema.Sequences.LANGUAGE_ID_SEQ);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			io.progix.dropwizard.jooq.schema.tables.Author.AUTHOR,
			io.progix.dropwizard.jooq.schema.tables.Book.BOOK,
			io.progix.dropwizard.jooq.schema.tables.BookStore.BOOK_STORE,
			io.progix.dropwizard.jooq.schema.tables.BookToBookStore.BOOK_TO_BOOK_STORE,
			io.progix.dropwizard.jooq.schema.tables.Language.LANGUAGE);
	}
}