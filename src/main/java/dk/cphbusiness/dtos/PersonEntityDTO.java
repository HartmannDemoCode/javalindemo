package dk.cphbusiness.dtos;

import dk.cphbusiness.controllers.PersonEntityController;
import dk.cphbusiness.persistence.model.Person;
import dk.cphbusiness.persistence.model.Phone;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonEntityDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> phones;
    private LocalDate birthDate;

    public PersonEntityDTO(String id, String firstName, String lastName, String email, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
    }
    public PersonEntityDTO(Person person) {
        this.id = person.getId().toString();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        this.birthDate = person.getBirthDate();
        this.phones = person.getPhones().stream().map(phone -> phone.getNumber()).toList();
    }
    public Person getEntity() {

        Person person = new Person();
        if(id!=null)
            person.setId(Integer.parseInt(id));
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(email);
        person.setBirthDate(birthDate);
        return person;
    }
}
