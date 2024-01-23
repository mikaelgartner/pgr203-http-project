package no.kristiania.http;

import no.kristiania.person.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    HttpServerTest() throws IOException {
    }

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }

    @Test
    void shouldRespondWith200ForKnownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/hello");
        assertAll(
                () -> assertEquals(200, client.getStatusCode()),
                () -> assertEquals("text/html", client.getHeader("Content-Type")),
                () -> assertEquals("<p>Hello world</p>", client.getMessageBody())
        );
    }

    @Test
    void shouldHandleMoreThanOneRequest() throws IOException {
        assertEquals(200, new HttpClient("localhost", server.getPort(), "/hello").getStatusCode());
        assertEquals(200, new HttpClient("localhost", server.getPort(), "/hello").getStatusCode());
    }

    @Test
    void shouldEchoQueryParameter() throws IOException {
        HttpClient client = new HttpClient(
                "localhost",
                server.getPort(),
                "/hello?firstName=Test&lastName=Persson");
        assertEquals("<p>Hello Persson, Test</p>", client.getMessageBody());
    }

    @Test
    void shouldServeFiles() throws IOException {
        String fileContent = "A file created at " + LocalTime.now();
        Files.write(Paths.get("target/test-classes/example-file.txt"), fileContent.getBytes());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/example-file.txt");
        assertEquals(fileContent, client.getMessageBody());
        assertEquals("text/plain", client.getHeader("Content-Type"));
    }

    @Test
    void shouldUseFileExtensionForContentType() throws IOException {
        String fileContent = "<p>Hello</p>";
        Files.write(Paths.get("target/test-classes/example-file.html"), fileContent.getBytes());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/example-file.html");
        assertEquals("text/html", client.getHeader("Content-Type"));
    }

    @Test
    void shouldReturnRolesFromServer() throws IOException, SQLException {
        RoleDao roleDao = new RoleDao(TestData.testDataSource());
        roleDao.save("Teacher");
        roleDao.save("Student");
        server.addController("/api/roleOptions", new RoleOptionsController(roleDao));

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/roleOptions");
        assertEquals(
                "<option value=1>Teacher</option><option value=2>Student</option>",
                client.getMessageBody()
        );
    }


    @Test
    void shouldListPeopleFromDatabase() throws SQLException, IOException {
        PersonDao personDao = new PersonDao(TestData.testDataSource());

        Person person1 = PersonDaoTest.examplePerson();
        personDao.save(person1);
        Person person2 = PersonDaoTest.examplePerson();
        personDao.save(person2);

        server.addController("/api/people", new ListPeopleController(personDao));

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/people");
        assertThat(client.getMessageBody())
                .contains(person1.getLastName() + ", " + person1.getFirstName())
                .contains(person2.getLastName() + ", " + person2.getFirstName())
                ;
    }

    @Test
    void shouldCreateNewPerson() throws IOException, SQLException {
        PersonDao personDao = new PersonDao(TestData.testDataSource());
        server.addController("/api/newPerson", new AddPersonController(personDao));


        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/newPerson",
                "lastName=Persson&firstName=Test"
        );
        assertEquals(200, postClient.getStatusCode());
        assertThat(personDao.listAll())
                .anySatisfy(p -> {
                    assertThat(p.getFirstName()).isEqualTo("Test");
                    assertThat(p.getLastName()).isEqualTo("Persson");
                });
    }
}