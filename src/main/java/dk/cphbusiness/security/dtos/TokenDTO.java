package dk.cphbusiness.security.dtos;

import lombok.*;

/**
 * Purpose: To hold information about a token
 * Author: Thomas Hartmann
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
public class TokenDTO {
    String token;
    String username;
}
