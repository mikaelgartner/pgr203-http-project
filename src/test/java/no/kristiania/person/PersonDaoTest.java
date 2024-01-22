package no.kristiania.person;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonDaoTest {

    private PersonDao dao = new PersonDao(TestData.testDataSource());

    @Test
    void shouldRetrieveSavedPerson() {
        Person person = examplePerson();
        dao.save(person);
        assertThat(dao.retrieve(person.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(person);
    }

    private Person examplePerson() {
        Person person = new Person();
        person.setFirstName(TestData.pickOne("Johannes", "Angelica", "Mikael", "Annie", "Sophie"));
        person.setLastName(TestData.pickOne("Jonsson", "Persson", "Olsson", "Fredriksson", "Jensen"));
        return person;
    }
}
