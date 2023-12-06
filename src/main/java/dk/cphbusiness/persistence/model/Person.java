package dk.cphbusiness.persistence.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name="Person.deleteAll", query="DELETE FROM Person")
})
public class Person implements IJPAEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Phone> phones = new HashSet<>();

    @ManyToMany
    @JoinTable( // owning side
            name = "person_hobby",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "hobby_name")
    )
    private Set<Hobby> hobbies = new HashSet<>();

    @ManyToOne
    private Address address;

    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Builder
    public Person(String firstName, String lastName, String email, LocalDate birthDate ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
    }

    @PrePersist
    private void prePersist() {
        this.creationDate = LocalDate.now();
        if (!validatePhoneNumbers(this.phones)) {
            throw new IllegalArgumentException("One or more phone numbers are invalid");
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (!validatePhoneNumbers(this.phones)) {
            throw new IllegalArgumentException("One or more phone numbers are invalid");
        }
    }

    private boolean validatePhoneNumbers(Set<Phone> phones) {
        for (Phone phone : phones) {
            if (!validatePhoneNumber(phone.getNumber())) {
                return false;
            }
        }

        return true;
    }

    public void addPhone(Phone phone) {
        this.phones.add(phone);
        phone.setPerson(this);
    }
    public void removePhone(Phone phone) {
        this.phones.remove(phone);
        phone.setPerson(null);
    }
    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return true;
        }

        return phoneNumber.matches("^[0-9]{8,11}$");
    }

    public void addHobby(Hobby hobby) {
        this.hobbies.add(hobby);
        hobby.getPersons().add(this);
    }
    public void removeHobby(Hobby hobby) {
        this.hobbies.remove(hobby);
        hobby.getPersons().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(email, person.email) && Objects.equals(creationDate, person.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, creationDate);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + "'" +
                ", lastName='" + lastName + "'" +
                ", email='" + email + "'" +
                ", birthDate=" + birthDate +
                ", creationDate=" + creationDate +
                '}';
    }
}