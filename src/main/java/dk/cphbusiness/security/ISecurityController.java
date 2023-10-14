package dk.cphbusiness.security;

import dk.cphbusiness.security.dtos.UserDTO;
import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import java.util.Set;

public interface ISecurityController {
    Handler login(); // to get a token
    Handler register(); // to get a user
    Handler authenticate(); // to verify a token
    boolean authorize(UserDTO userDTO, Set<String> allowedRoles); // to verify user roles
    String createToken(UserDTO user) throws Exception;
    UserDTO verifyToken(String token) throws Exception;
    String renewToken(String token, int minutesToExpire) throws Exception;
}
