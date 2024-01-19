package dk.cphbusiness.dtos;

import dk.cphbusiness.persistence.model.Phone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhoneDTO {
    String number;
    String description;
    public PhoneDTO(Phone phone) {
        this.number = phone.getNumber();
        this.description = phone.getDescription();
    }
}
