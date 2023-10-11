package dk.cphbusiness.security.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
public class TokenDTO {
    String token;
    String username;
}
