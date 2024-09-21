package dk.cphbusiness.security;

import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.entities.Role;
import dk.cphbusiness.security.entities.User;
import dk.cphbusiness.security.exceptions.ValidationException;
import jakarta.persistence.*;

import java.util.*;


/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            System.out.println("USERNAME INSIDE GET_VERIFIED_USER: " + username + " PASSWORD: " + password);
            List<User> users = em.createQuery("SELECT u FROM User u").getResultList();
            System.out.println("SIZE OF USERS: " + users.size());
            users.stream().forEach(user -> System.out.println(user.getUsername() + " " + user.getPassword()));
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
    public User createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, username);
            if (userEntity != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            userEntity = new User(username, password);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null)
                userRole = new Role("user");
                em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }
}
