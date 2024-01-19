package dk.cphbusiness.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@NamedQueries({
    @NamedQuery(name="Address.deleteAll", query="DELETE FROM Address")
})
public class Address implements IJPAEntity<Integer>, IAssociableEntity<Person>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "street", nullable = false)
    private String street;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Zip zip;

    @OneToMany(mappedBy = "address")
    @ToString.Exclude
    private final Set<Person> persons = new HashSet<>();

    public Address(String street, Zip zip) {
        this.street = street;
        this.zip = zip;
    }

    @Override
    public void addAssociation(Person entity) {
        this.persons.add(entity);
        entity.setAddress(this);
    }

    @Override
    public void removeAssociation(Person entity) {
        this.persons.remove(entity);
        entity.setAddress(null);
    }
}