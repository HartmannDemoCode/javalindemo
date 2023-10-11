package dk.cphbusiness.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.dtos.TokenDTO;
import dk.cphbusiness.security.dtos.UserDTO;
import dk.cphbusiness.security.exceptions.ValidationException;
import io.javalin.http.Handler;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {
    ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static ISecurityDAO securityDAO;
    private SecurityController() { }
    private static SecurityController instance;
    public static SecurityController getInstance() {
        if (instance == null) {
            securityDAO = SecurityDAO.getInstance(HibernateConfig.getEntityManagerFactory());
            instance = new SecurityController();
        }
        return instance;
    }
    @Override
    public Handler login() {
        return (ctx) -> {
           ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                UserDTO user = ctx.bodyAsClass(UserDTO.class);
                User verifiedUserEntity = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                String token = securityDAO.createToken(verifiedUserEntity);
                ctx.status(200).json(new TokenDTO(token, user.getUsername()));
            } catch (EntityNotFoundException | ValidationException e) {
                ctx.status(401);
                ctx.json(returnObject.put("msg",e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException(500, "Internal server error");
            }
        };
    }

    @Override
    public Handler logout() {
        // How to logout? maintain a blacklist of revoked tokens?
        return (ctx) -> {
            ctx.status(200);
            ctx.json("You have been logged out, but your token will be valid untill it expires");
        };
    }

    @Override
    public Handler register() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
                User created = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());
                String token = securityDAO.createToken(created);
                ctx.status(200).json(new TokenDTO(token, userInput.getUsername()));
            } catch (EntityExistsException e) {
                ctx.status(409);
                ctx.json(returnObject.put("msg",e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException(500, "Internal server error");
            }
        };
    }

    @Override
    public Handler authenticate() {
        // Purpose: before filter -> Check for Authorization header, find user inside the token, forward the ctx object with username on attribute
        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            try {
                String header = ctx.header("Authorization");
                if(header == null)
                    ctx.status(401).json(returnObject.put("msg","Authorization header missing"));
                String token = header.split(" ")[1];
                if (token == null) {
                    ctx.status(401).json(returnObject.put("msg","Authorization header malformed"));
                }
                User verifiedTokenUserEntity = securityDAO.verifyToken(token);
                    if (verifiedTokenUserEntity == null) {
                        ctx.status(401).json(returnObject.put("msg","Invalid User or Token"));
                    }
                String userName = verifiedTokenUserEntity.getUsername();
                ctx.attribute("userName", userName);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException(500,"Something went wrong");
            }
        };
    }

    @Override
    public boolean authorize(String username, Set<String> allowedRoles) {
        // Called from the ApplicationConfig.setSecurityRoles

        User user = securityDAO.getUser(username);
        System.out.println("USER FROM TOKEN: "+user+" roles: "+user.getRoles());
        AtomicBoolean hasAccess = new AtomicBoolean(false); // Since we update this in a lambda expression, we need to use an AtomicBoolean
        if (user != null) {
            user.getRoles().stream().forEach(role -> {
                if (allowedRoles.contains(role.getRoleName().toUpperCase())) {
                    hasAccess.set(true);
                }
            });
        }
        return hasAccess.get();
    }
}
