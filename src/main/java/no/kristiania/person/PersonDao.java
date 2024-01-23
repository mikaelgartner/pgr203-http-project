package no.kristiania.person;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonDao {
    private final DataSource dataSource;

    public PersonDao(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public void save(Person person) throws SQLException {

        Connection connection = dataSource.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into people (first_name, last_name) values (?, ?)",
                Statement.RETURN_GENERATED_KEYS
                )) {
            statement.setString(1, person.getFirstName());
            statement.setString(2, person.getLastName());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                person.setId(rs.getLong("id"));
            }
        }
    }

    public Person retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from people where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    return readFromResultSet(rs);
                }
            }
        }
    }

    public List<Person> listAll() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from people")) {
                try (ResultSet rs = statement.executeQuery()) {
                    ArrayList<Person> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(readFromResultSet(rs));
                    }
                    return result;
                }
            }
        }
    }

    private static Person readFromResultSet(ResultSet rs) throws SQLException {
        Person person = new Person();
        person.setId(rs.getLong("id"));
        person.setFirstName(rs.getString("first_name"));
        person.setLastName(rs.getString("last_name"));
        return person;
    }
}
