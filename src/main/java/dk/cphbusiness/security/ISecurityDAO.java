package dk.cphbusiness.security;
import dk.bugelhartmann.UserDTO;
import dk.cphbusiness.security.entities.User;
import dk.cphbusiness.exceptions.ValidationException;

/**
 * Purpose: To handle security with the database
 * Author: Thomas Hartmann
 */
public interface ISecurityDAO {
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    UserDTO createUser(String username, String password);
    UserDTO addRoleToUser(String username, String role);
    UserDTO removeRoleFromUser(String username, String role);
}
