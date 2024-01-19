package dk.cphbusiness.dtos;

import dk.cphbusiness.persistence.model.Address;
import dk.cphbusiness.persistence.model.Zip;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDTO {
    private String id;
    private String street;
    private String city;
    private String zipCode;
    private Integer[] residents;

    public AddressDTO(Address address) {
        this.id = address.getId().toString();
        this.street = address.getStreet();
        this.city = address.getZip().getCityName();
        this.zipCode = address.getZip().getZip();
        if(address.getPersons()!=null)
            this.residents = address.getPersons().stream().map(person -> person.getId()).toArray(Integer[]::new);
    }

    public Address getEntity() {
        Address address = new Address();
        if(id!=null)
            address.setId(Integer.parseInt(id));
        address.setStreet(street);
        address.setZip(new Zip(city, zipCode));
        return address;
    }

    public static Set<AddressDTO> getEntities(Set<Address> addresses) {
        return addresses.stream().map(address -> new AddressDTO(address)).collect(Collectors.toSet());
    }
}
