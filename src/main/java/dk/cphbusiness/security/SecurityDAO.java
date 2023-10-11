package dk.cphbusiness.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.exceptions.NotAuthorizedException;
import dk.cphbusiness.security.exceptions.ValidationException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityDAO implements ISecurityDAO, IValidatable<String> {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;
    private SecurityDAO() {}
    public static ISecurityDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SecurityDAO();
        }
        return instance;
    }
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = getEntityManager()){
            User user = em.find(User.class, username);
            if (user == null)
                 throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return user;
        }
    }

    @Override
    public Role createRole(String role) {
        try(EntityManager em = getEntityManager()) {
            Role roleEntity = em.find(Role.class, role);
            if(roleEntity != null)
                return roleEntity;

            roleEntity = new Role(role);
            em.getTransaction().begin();
            em.persist(roleEntity);
            em.getTransaction().commit();
            return roleEntity;
        }
    }

    @Override
    public User createUser(String username, String password) {
        try(EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, username);
            if(userEntity != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            Role userRole = em.find(Role.class, "user");
            if(userRole == null)
                userRole = createRole("user");
            userEntity = new User(username, password);
            userEntity.addRole(userRole);
            em.getTransaction().begin();
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        }
    }

    @Override
    public User getUser(String userName){
        try(EntityManager em = getEntityManager()) {
            User user = em.createQuery("SELECT u FROM User u JOIN u.roles WHERE u.username = :username",User.class).setParameter("username",userName).getSingleResult();
            if(user == null)
                throw new EntityNotFoundException("No user found with username: " + userName);
            return user;
        }
    }

    @Override
    public User addUserRole(String username, String role) {
        try(EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if(user == null)
                throw new EntityNotFoundException("No user found with username: " + username);
            Role roleEntity = em.find(Role.class, role);
            if(roleEntity == null)
                throw new EntityNotFoundException("No role found with name: " + role);
            if(user.getRoles().contains(roleEntity))
                return user;
            em.getTransaction().begin();
            user.getRoles().add(roleEntity);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public User removeUserRole(String username, String role) {
        try(EntityManager em = getEntityManager()){
            User user = em.find(User.class, username);
            if(user == null)
                throw new EntityNotFoundException("No user found with username: " + username);
            Role roleEntity = em.find(Role.class, role);
            if(roleEntity == null)
                throw new EntityNotFoundException("No role found with name: " + role);
            if(!user.getRoles().contains(roleEntity))
                return user;
            em.getTransaction().begin();
            user.getRoles().remove(roleEntity);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public boolean hasRole(String role, User userEntity) {
        try(EntityManager em = getEntityManager()){
            Role roleEntity = em.find(Role.class, role);
            if(roleEntity == null)
                throw new EntityNotFoundException("No role found with name: " + role);
            return userEntity.getRoles().contains(roleEntity);
        }
    }

    @Override
    public String createToken(User user) throws Exception {
        String ISSUER;
        String TOKEN_EXPIRE_TIME;
        String SECRET_KEY;

        boolean isDeployed = (System.getenv("DEPLOYED") != null);

        if (isDeployed) {
            ISSUER = System.getenv("ISSUER");
            TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
            SECRET_KEY = System.getenv("SECRET_KEY");
        } else {
            ISSUER = "Thomas Hartmann";
            TOKEN_EXPIRE_TIME = "1800000";
            SECRET_KEY = readProp("SECRET_KEY");
        }
        try {
            // https://codecurated.com/blog/introduction-to-jwt-jws-jwe-jwa-jwk/

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(ISSUER)
                    .claim("username", user.getUsername())
                    .claim("roles", user.getRolesAsStrings().stream().reduce((s1, s2) -> s1 + "," + s2).get())
                    .expirationTime(new Date(new Date().getTime() + Integer.parseInt(TOKEN_EXPIRE_TIME)))
                    .build();
            Payload payload = new Payload(claimsSet.toJSONObject());

            JWSSigner signer = new MACSigner(SECRET_KEY);
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(jwsHeader, payload);
            jwsObject.sign(signer);
            return jwsObject.serialize();

        } catch (JOSEException e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token");
        }
    }

    @Override
    public User verifyToken(String token) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean isDeployed = (System.getenv("DEPLOYED") != null);
            String SECRET = isDeployed ? System.getenv("SECRET_KEY") : readProp("SECRET_KEY");

            JWSVerifier verifier = new MACVerifier(SECRET);

            if (signedJWT.verify(verifier)) {
                if (new Date().getTime() > signedJWT.getJWTClaimsSet().getExpirationTime().getTime()) {
                    throw new NotAuthorizedException(403, "Your Token is no longer valid");
                }

                return jwt2user(signedJWT);

            } else {
                throw new JOSEException("UserEntity could not be extracted from token");
            }
        } catch (ParseException | JOSEException e) {
            throw new NotAuthorizedException(403, "Could not validate token");
        }
    }
    private User jwt2user(SignedJWT jwt) throws ParseException {
        String roles = jwt.getJWTClaimsSet().getClaim("roles").toString();
        String username = jwt.getJWTClaimsSet().getClaim("username").toString();

        Set<Role> rolesSet = Arrays
                .stream(roles.split(","))
                .map(role -> new Role(role))
                .collect(Collectors.toSet());
        return new User(username, rolesSet);
    }

    @Override
    public String reNewToken(String token, int minutesToExpire) throws Exception {
        // if time to expire is less than `minutesToExpire` then renew token

        int MILLISECONDS_TO_EXPIRE = minutesToExpire * 60 * 1000;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean isDeployed = (System.getenv("DEPLOYED") != null);
            String SECRET = isDeployed ? System.getenv("SECRET_KEY") : readProp("SECRET_KEY");

            JWSVerifier verifier = new MACVerifier(SECRET);

            if (signedJWT.verify(verifier)) {
                if (new Date().getTime() > signedJWT.getJWTClaimsSet().getExpirationTime().getTime() - MILLISECONDS_TO_EXPIRE) {
                    return createToken(jwt2user(signedJWT));
                }
                return token;
            } else {
                throw new JOSEException("UserEntity could not be extracted from token");
            }
        } catch (ParseException | JOSEException e) {
            throw new NotAuthorizedException(403, "Could not validate token");
        }
    }

    @Override
    public boolean validateId(String id) {
        try(EntityManager em = getEntityManager()) {
            User user = em.find(User.class,id);
            if (user == null)
                return false;
            return true;
        }
    }

    private String readProp(String propName) throws Exception {
        // Read the property file if not deployed (else read system vars instead)
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(is);
            return prop.getProperty(propName);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new Exception("Could not read property");
        }
    }
}
