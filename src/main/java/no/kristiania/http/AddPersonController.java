package no.kristiania.http;

import no.kristiania.person.Person;
import no.kristiania.person.PersonDao;

import java.sql.SQLException;
import java.util.Map;

public class AddPersonController implements HttpController {
    private final PersonDao personDao;

    public AddPersonController(PersonDao personDao) {
        this.personDao = personDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        Person person = new Person();
        person.setFirstName(queryMap.get("firstName"));
        person.setLastName(queryMap.get("lastName"));

        if (person.getFirstName() == null || person.getFirstName().isBlank()) {
            return new HttpMessage("HTTP/1.1 400 request error", "First name must be provided");
        } else if (person.getLastName() == null || person.getLastName().isBlank()) {
            return new HttpMessage("HTTP/1.1 400 request error", "Last name must be provided");
        }

        personDao.save(person);

        return new HttpMessage("HTTP/1.1 200 OK", "Person successfully added to the database");
    }
}
