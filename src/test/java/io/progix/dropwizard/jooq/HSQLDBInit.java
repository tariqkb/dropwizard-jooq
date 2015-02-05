package io.progix.dropwizard.jooq;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

import static io.progix.dropwizard.jooq.schema.Tables.AUTHOR;

public class HSQLDBInit {

    public static void init(Map<String, String> properties) throws Exception {
        String url = properties.get("url");
        String username = properties.get("username");
        String password = properties.get("password");

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection conn = DriverManager.getConnection(url, connectionProps);

        DSLContext jooq = DSL.using(conn);

        jooq.createTable(AUTHOR).column(AUTHOR.ID, SQLDataType.INTEGER).column(AUTHOR.NAME, SQLDataType.VARCHAR).execute();

        jooq.insertInto(AUTHOR).set(AUTHOR.ID, 1).set(AUTHOR.NAME, "Tariq").execute();

        conn.commit();
        conn.close();
    }

}
