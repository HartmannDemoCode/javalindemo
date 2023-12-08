package dk.cphbusiness.persistence.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
public class Hobby implements IJPAEntity<String>, IAssociableEntity<Person>{

    @Id
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private HobbyCategory category;

    @ManyToMany(mappedBy = "hobbies")
    @ToString.Exclude
    private final Set<Person> persons = new HashSet<>();

    public String getId() {
        return name;
    }

    @Builder
    public Hobby(String name, HobbyCategory category ) {
        this.name = name;
        this.category = category;
    }

    @Override
    public void addAssociation(Person entity) {
        this.persons.add(entity);
        entity.getHobbies().add(this);
    }

    @Override
    public void removeAssociation(Person entity) {
        this.persons.remove(entity);
        entity.getHobbies().remove(this);
    }
    @Getter
    public enum HobbyCategory {
        EDUCATIONAL("Educational"),
        GENERAL("General"),
        COLLECTION_HOBBY("Collecting stuff"),
        COMPETITION("Competition");

        private final String name;

        HobbyCategory(String name) {
            this.name = name;
        }
    }
}