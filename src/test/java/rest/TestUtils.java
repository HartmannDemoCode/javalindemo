package rest;

import dk.cphbusiness.persistence.model.*;
import dk.cphbusiness.security.Role;
import dk.cphbusiness.security.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    // method to create users and roles before each test
    public void createUsersAndRoles(EntityManagerFactory emfTest) {
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User u").executeUpdate();
            em.createQuery("DELETE FROM Role r").executeUpdate();
            User user = new User("user", "user123");
            User admin = new User("admin", "admin123");
            User superUser = new User("super", "super123");
            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            user.addRole(userRole);
            admin.addRole(adminRole);
            superUser.addRole(userRole);
            superUser.addRole(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(superUser);
            em.persist(userRole);
            em.persist(adminRole);
            em.getTransaction().commit();
        }
    }

    public Map<String, IJPAEntity> createPersonEntities(EntityManagerFactory emfTest) {
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Phone").executeUpdate();
            em.createQuery("DELETE FROM Person").executeUpdate();
            em.createQuery("DELETE FROM Address").executeUpdate();
            em.createQuery("DELETE FROM Zip").executeUpdate();
            em.createQuery("DELETE FROM Hobby").executeUpdate();
            Person p1 = new Person("Hans", "Hansen", "hans@gmail.com", LocalDate.now());
            Person p2 = new Person("Grethe", "Hansen", "grethe@gmail.com", LocalDate.now());
            Person p3 = new Person("Heksen", "Hansen", "heks@gmail.com", LocalDate.now());
            Zip z1 = new Zip("2300", "København S");
            Zip z2 = new Zip("2400", "København NV");
            Zip z3 = new Zip("2500", "Valby");
            Address a1 = new Address("Hansvej 1", z1);
            Address a2 = new Address("Grethevej 2", z2);
            Address a3 = new Address("Heksevej 3", z3);
            a1.addAssociation(p1);
            a1.addAssociation(p2); // Hans and Grethe lives on the same address
            a2.addAssociation(p3);
            Phone ph1 = new Phone("12345678", "Hans' telefon");
            Phone ph2 = new Phone("87654321", "Grethes telefon");
            Phone ph3 = new Phone("11111111", "Heksens telefon");
            Phone ph4 = new Phone("20666666", "Heksens mobil");
            ph1.addAssociation(p1);
            ph2.addAssociation(p2);
            ph3.addAssociation(p3);
            ph4.addAssociation(p3);
            Hobby h1 = new Hobby("Fodbold", Hobby.HobbyCategory.COMPETITION);
            Hobby h2 = new Hobby("Håndbold", Hobby.HobbyCategory.COMPETITION);
            Hobby h3 = new Hobby("Kahoot", Hobby.HobbyCategory.EDUCATIONAL);
            h1.addAssociation(p1);
            h1.addAssociation(p2);
            h2.addAssociation(p2);
            h2.addAssociation(p3);
            h3.addAssociation(p3);
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.persist(z1);
            em.persist(z2);
            em.persist(z3);
            em.persist(a1);
            em.persist(a2);
            em.persist(a3);
            em.persist(ph1);
            em.persist(ph2);
            em.persist(ph3);
            em.persist(ph4);
            em.persist(h1);
            em.persist(h2);
            em.persist(h3);
            em.getTransaction().commit();
            Map<String, IJPAEntity> entityMap = new HashMap<>();
            return Map.of("Person1", p1 // MAX 10 entries this way (alternatively use Map.ofEntries(Map.entry("Person1", p1))
                    , "Person2", p2
                    , "Person3", p3
                    , "address1", a1
                    , "address2", a2
                    , "address3", a3
                    , "phone1", ph1
                    , "phone2", ph2
                    , "phone3", ph3
                    , "hobby1", h1
//                    , "hobby2", h2
//                    , "hobby2", h2
//                    , "hobby3", h3
            );
        }
    }
}

