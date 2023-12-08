package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.persistence.model.IJPAEntity;
import dk.cphbusiness.persistence.model.Person;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

/**
 * This class is a generic DAO (Data Access Object) that can be used to perform CRUD operations on any entity.
 * @param <T> The entity class that the DAO should be used for.
 */
public class DAO<T extends IJPAEntity> extends ADAO<T> {

    public DAO(Class<T> entityClass, EntityManagerFactory emf) {
        super(entityClass, emf);
    }

    public static void main(String[] args) {
        DAO<Person> personDAO = new DAO<Person>(Person.class, HibernateConfig.getEntityManagerFactory());
        personDAO.create(new Person("test", "test", "test@mail.com", LocalDate.now()));
        personDAO.getAll().forEach(System.out::println);
    }

}
