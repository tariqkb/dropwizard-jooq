package io.progix.dropwizard.jooq;

import org.jooq.DSLContext;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

import static io.progix.dropwizard.jooq.schema.Tables.AUTHOR;

public class HSQLDBInit {

    public static void initPublic(Map<String, String> properties) throws Exception {
        String url = properties.get("url");
        String username = properties.get("username");
        String password = properties.get("password");

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection conn = DriverManager.getConnection(url, connectionProps);

        DSLContext jooq = DSL.using(conn);

        jooq.createTable(AUTHOR).column(AUTHOR.ID, SQLDataType.INTEGER).column(AUTHOR.NAME, SQLDataType.VARCHAR)
                .execute();

        jooq.execute("ALTER TABLE PUBLIC.author ADD PRIMARY KEY (id)");

        jooq.insertInto(AUTHOR).set(AUTHOR.ID, 1).set(AUTHOR.NAME, "Tariq").execute();

        jooq.execute("CREATE SCHEMA BUGRARA;");

        conn.commit();

        jooq = DSL.using(conn, new Settings().withRenderMapping(
                new RenderMapping().withSchemata(new MappedSchema().withInput("PUBLIC").withOutput("BUGRARA"))));

        jooq.createTable(AUTHOR).column(AUTHOR.ID, SQLDataType.INTEGER).column(AUTHOR.NAME, SQLDataType.VARCHAR)
                .execute();

        jooq.execute("ALTER TABLE BUGRARA.author ADD PRIMARY KEY (id)");

        jooq.insertInto(AUTHOR).set(AUTHOR.ID, 1).set(AUTHOR.NAME, "Narmeen").execute();

        conn.commit();

        conn.close();

    }

}
