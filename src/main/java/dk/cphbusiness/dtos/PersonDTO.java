package dk.cphbusiness.dtos;

import dk.cphbusiness.persistence.model.Person;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Getter
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
        if(person.getId()!=null)
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

    public void setId(String id) {
        this.id = id;
    }
    public Person toEntity() {
        Person person = Person.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .birthDate(birthDate)
                .build();
        if(id!=null)
            person.setId(Integer.parseInt(id));
        return person;
    }
    public static Set<PersonDTO> getEntities(Set<Person> persons) {
        return persons.stream().map(person -> new PersonDTO(person)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "PersonDTO{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phones=" + phones +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", hobbies=" + hobbies +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return false;
        if(obj.getClass()!=this.getClass())
            return false;
        PersonDTO other = (PersonDTO) obj;
        return this.id.equals(other.id)
                && this.firstName.equals(other.firstName)
                && this.lastName.equals(other.lastName)
                && this.email.equals(other.email)
//                && this.address.equals(other.address)
//                && this.hobbies.equals(other.hobbies)
//                && this.phones.equals(other.phones)
                && this.birthDate.equals(other.birthDate);
    }
}
