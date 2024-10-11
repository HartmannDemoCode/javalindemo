package dk.cphbusiness.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.*;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.entities.User;
import dk.cphbusiness.security.exceptions.NotAuthorizedException;
import dk.cphbusiness.security.exceptions.ValidationException;
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
//            catch (Exception e) {
//                e.printStackTrace();
//                throw new ApiException(500, "Internal server error");
//            }
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

    @Override
    public Handler authenticate() {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) -> {
            // This is a preflight request => OK
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            String header = ctx.header("Authorization");
            // If there is no token we do not allow entry
            if (header == null) {
                ctx.status(401).json(returnObject.put("msg", "Authorization header missing"));
                return;
            }
            String token = header.split(" ")[1];
            // If the Authorization Header was malformed = no entry
            if (token == null) {
                ctx.status(400).json(returnObject.put("msg", "Authorization header malformed"));
                return;
            }
            UserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null) {
                ctx.status(403).json(returnObject.put("msg", "Invalid User or Token"));
                return;
            }
            System.out.println("USER IN AUTHENTICATE: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    @Override
    public Handler authorize() {
        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            // If the endpoint is not protected with any roles:
            if (ctx.routeRoles().isEmpty())
                return;

            // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
            Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
            if (allowedRoles.contains("ANYONE")) {
                return;
            }
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null) {
                throw new UnauthorizedResponse("No user was added from the token");
            }

            // 3. See if any role matches
            AtomicBoolean hasAccess = new AtomicBoolean(false); // Since we update this in a lambda expression, we need to use an AtomicBoolean
            user.getRoles().stream().forEach(role -> {
                if (allowedRoles.contains(role.toUpperCase())) {
                    hasAccess.set(true);
                }
            });
            if (!hasAccess.get())
                throw new UnauthorizedResponse("User was not authorized with roles: "+user.getRoles()+". Needed roles are: "+allowedRoles);
        };
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
                    ISSUER = "Thomas Hartmann";
                    TOKEN_EXPIRE_TIME = "1800000"; // 30 min
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