package dk.cphbusiness.security;
import dk.cphbusiness.security.entities.User;
import dk.cphbusiness.security.exceptions.ValidationException;

/**
 * Purpose: To handle security with the database
 * Author: Thomas Hartmann
 */
public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException;
    User createUser(String username, String password);
}
