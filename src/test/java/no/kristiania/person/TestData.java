package no.kristiania.person;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class TestData {
    public static DataSource testDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:person;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
