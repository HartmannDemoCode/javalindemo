package dk.cphbusiness.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.*;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.entities.User;
import dk.cphbusiness.exceptions.NotAuthorizedException;
import dk.cphbusiness.exceptions.ValidationException;
import dk.cphbusiness.utils.Utils;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.text.ParseException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityController implements ISecurityController {
    ObjectMapper objectMapper = new ObjectMapper();
    ITokenSecurity tokenSecurity = new TokenSecurity();
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;

    private SecurityController() {
    }


    public static SecurityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public Handler login() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            try {
                UserDTO user = ctx.bodyAsClass(UserDTO.class);
                UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                String token = createToken(verifiedUser);

                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("username", verifiedUser.getUsername()));

            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                System.out.println(e.getMessage());
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }

    @Override
    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
                User created = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());

                String token = createToken(new UserDTO(created.getUsername(), Set.of("USER")));
                ctx.status(HttpStatus.CREATED).json(returnObject
                        .put("token", token)
                        .put("username", created.getUsername()));
            } catch (EntityExistsException e) {
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
                ctx.json(returnObject.put("msg", "User already exists"));
            }
        };
    }

    /**
     * Purpose: For a user to prove who they are with a valid token
     * @return
     */
    @Override
    public Handler authenticate() {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) -> {
            // This is a preflight request => no need for authentication
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            // If the endpoint is not protected with roles or is open to ANYONE role, then skip
            Set<String> allowedRoles = ctx.routeRoles().stream().map(role->role.toString().toUpperCase()).collect(Collectors.toSet());
            if(isOpenEndpoint(allowedRoles))
                return;

            // If there is no token we do not allow entry
            String header = ctx.header("Authorization");
            if (header == null) {
//                throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is missing");
            }

            // If the Authorization Header was malformed, then no entry
            String token = header.split(" ")[1];
            if (token == null) {
                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is malformed");

            }
            UserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null) {
                throw new dk.cphbusiness.exceptions.ApiException(401, "Invalid user or token");
            }
            ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    /**
     * Purpose: To check if the Authenticated user has the rights to access a protected endpoint
     * @return
     */
    @Override
    public Handler authorize() {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) -> {
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
            if (isOpenEndpoint(allowedRoles))
                return;
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null) {
//                throw new UnauthorizedResponse("No user was added from the token");
                throw new dk.cphbusiness.exceptions.ApiException(401, "No user was added from token");
            }

            // 3. See if any role matches
            if(!userHasAllowedRole(user, allowedRoles))
                throw new ApiException(403,"User was not authorized with roles: "+ user.getRoles()+". Needed roles are: "+ allowedRoles);

        };
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }


    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }

    @Override
        public String createToken (UserDTO user){
            try {
                String ISSUER;
                String TOKEN_EXPIRE_TIME;
                String SECRET_KEY;

                if (System.getenv("DEPLOYED") != null) {
                    ISSUER = System.getenv("ISSUER");
                    TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                    SECRET_KEY = System.getenv("SECRET_KEY");
                } else {
                    ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                    TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                    SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
                }
                return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException(500, "Could not create token");
            }
        }

        @Override
        public UserDTO verifyToken (String token){
            boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
            String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

            try {
                if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                    return tokenSecurity.getUserWithRolesFromToken(token);
                } else {
                    throw new NotAuthorizedException(403, "Token is not valid");
                }
            } catch (ParseException | NotAuthorizedException | TokenVerificationException e) {
                e.printStackTrace();
                throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
            }
        }
    }