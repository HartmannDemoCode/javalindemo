package dk.cphbusiness.persistence.daos;


import dk.cphbusiness.persistence.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Set;

/**
 * Purpose: This class is a specific DAO (Data Access Object) that can be used to perform CRUD operations on the Person entity plus some extra queries.
 * @param <T> The entity class that the DAO should be used for.
 * Author: Thomas Hartmann
 */
public class PersonDAO extends DAO<Person> {

    public PersonDAO(Class<Person> entityClass, EntityManagerFactory emf) {
        super(entityClass, emf);
    }

    public Set<Phone> getPhoneNumbers(int id) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            entityManager.getTransaction().begin();
            Person person = entityManager.find(Person.class, id);
            entityManager.getTransaction().commit();
            return person.getPhones();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public List<Person> getAllByZip(Integer zip) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            entityManager.getTransaction().begin();
            List<Person> persons = entityManager.createQuery("SELECT p FROM Person p LEFT JOIN p.address address WHERE address IS NOT NULL OR address.zip.zip = :zip", Person.class)
                    .setParameter("zip", zip)
                    .getResultList();
            entityManager.getTransaction().commit();
            return persons;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public Person getByPhone(String number) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            entityManager.getTransaction().begin();
            Person person = entityManager.createQuery("SELECT p FROM Person p JOIN p.phones ph WHERE ph.number = :number", Person.class)
                    .setParameter("number", number)
                    .getSingleResult();
            entityManager.getTransaction().commit();
            return person;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public List<Person> getAllByHobby(Hobby hobby) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            entityManager.getTransaction().begin();
            List<Person> persons = entityManager.createQuery("SELECT DISTINCT p FROM Person p JOIN p.hobbies ph WHERE ph.hobby = :hobby", Person.class)
                    .setParameter("hobby", hobby)
                    .getResultList();
            entityManager.getTransaction().commit();
            return persons;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public Person getPersonByEmail(String mail) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            entityManager.getTransaction().begin();
            Person person = entityManager.createQuery("SELECT p FROM Person p WHERE p.email = :mail", Person.class)
                    .setParameter("mail", mail)
                    .getSingleResult();
            entityManager.getTransaction().commit();
            return person;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    public Address getAddressById(int id) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            Address address = entityManager.find(Address.class, id);
            return address;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    public Phone getPhoneById(String id) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            Phone phone = entityManager.find(Phone.class, id);
            return phone;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    public Hobby getHobbyById(String id) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            Hobby hobby = entityManager.find(Hobby.class, id);
            return hobby;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    public Zip getZipById(String id) {
        try (EntityManager entityManager = super.getEntityManagerFactory().createEntityManager()) {
            Zip zip = entityManager.find(Zip.class, id);
            return zip;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}