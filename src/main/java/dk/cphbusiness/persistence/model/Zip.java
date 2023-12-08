package dk.cphbusiness.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name="Zip.deleteAll", query = "DELETE from Zip"),
        @NamedQuery(name="Zip.getAll", query = "SELECT z from Zip z"),
        @NamedQuery(name="Zip.getByZip", query = "SELECT z from Zip z WHERE z.zip = :zip"),
})
@Table(name = "zip")
public class Zip implements IJPAEntity<String>, IAssociableEntity<Address>{

    @Id
    @Column(name = "zip")
    private String zip;

    @Column(name = "city_name")
    private String cityName;

    @OneToMany(mappedBy = "zip")
    @ToString.Exclude
    private Set<Address> address = new HashSet<>();

    public String getId() {
        return zip;
    }

    public Zip(String zip, String city) {
        this.zip = zip;
        this.cityName = city;
    }

    @Override
    public void addAssociation(Address entity) {
        this.address.add(entity);
        entity.setZip(this);
    }

    @Override
    public void removeAssociation(Address entity) {
        this.address.remove(entity);
        entity.setZip(null);
    }
}