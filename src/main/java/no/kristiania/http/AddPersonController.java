package no.kristiania.http;

import no.kristiania.person.PersonDao;

import java.sql.SQLException;

public class AddPersonController implements HttpController {
    public AddPersonController(PersonDao personDao) {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        return new HttpMessage("HTTP/1.1 200 OK", "It is done");
    }
}
