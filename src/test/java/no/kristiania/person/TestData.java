package no.kristiania.person;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class TestData {
    public static DataSource testDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:person");
        return dataSource;
    }
}
