package dk.cphbusiness.security;
import dk.cphbusiness.security.Role;
import dk.cphbusiness.security.exceptions.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException;
    Role createRole(String role);
    User createUser(String username, String password);
    User getUser(String username);
    User addUserRole(String username, String role);
    User removeUserRole(String username, String role);
    boolean hasRole(String role, User userEntity);
    String createToken(User user) throws Exception;
    User verifyToken(String token) throws Exception;
    String reNewToken(String token, int minutesToExpire) throws Exception;
}
