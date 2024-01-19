package dk.cphbusiness.security;

import java.util.Set;

/**
 * Purpose: to set methods for handling security with the user
 * Author: Thomas Hartmann
 */
public interface ISecurityUser {
    Set<String>  getRolesAsStrings();
    boolean verifyPassword(String pw);
    void addRole(Role role);
    void removeRole(String role);
}
