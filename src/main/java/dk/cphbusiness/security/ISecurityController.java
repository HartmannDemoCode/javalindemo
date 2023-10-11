package dk.cphbusiness.security;

import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import java.util.Set;

public interface ISecurityController {
    Handler login(); // to get a token
    Handler logout();
    Handler register(); // to get a user
    Handler authenticate(); // to verify a token
    boolean authorize(String username, Set<String> allowedRoles); // to verify user roles
}
