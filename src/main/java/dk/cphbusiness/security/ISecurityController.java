package dk.cphbusiness.security;

import dk.bugelhartmann.UserDTO;
import io.javalin.http.Handler;

import java.util.Set;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public interface ISecurityController {
    Handler login(); // to get a token
    Handler register(); // to get a user
    Handler authenticate(); // to verify roles inside token
    Handler authorize();
    Handler verify(); // to verify a token
    Handler timeToLive(); // to check how long a token is valid
}
