package dk.cphbusiness.dtos;

import dk.cphbusiness.persistence.model.Person;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<PhoneDTO> phones;
    private LocalDate birthDate;
    private String address;
    private Set<String> hobbies;

    public PersonDTO(String id, String firstName, String lastName, String email, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
    }
    public PersonDTO(Person person) {
        this.id = person.getId().toString();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        this.birthDate = person.getBirthDate();
        if(person.getPhones()!=null)
            this.phones = person.getPhones().stream().map(phone -> new PhoneDTO(phone)).toList();
        if(person.getHobbies()!=null)
            this.hobbies = person.getHobbies().stream().map(hobby -> hobby.getName()).collect(Collectors.toSet());
        if(person.getAddress()!=null)
            this.address = person.getAddress().getStreet()+ ", " + person.getAddress().getZip().getZip() + " " + person.getAddress().getZip().getCityName();
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
    public static Set<PersonDTO> getEntities(Set<Person> persons) {
        return persons.stream().map(person -> new PersonDTO(person)).collect(Collectors.toSet());
    }
}
