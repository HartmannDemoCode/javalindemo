package dk.cphbusiness.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimplePersonDTO {
    private String name;
    private int age;
}
