package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.persistence.model.IJPAEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;

import javax.lang.model.UnknownEntityException;
import java.util.List;

/**
 * This is an abstract class that is used to perform CRUD operations on any entity. It can be extended to gain access to basic CRUD operations.
 *
 * @param <T>
 */
abstract class ADAO<T extends IJPAEntity> implements IDAO<T> {

    private EntityManagerFactory emf;
    private final Class<T> entityType;
    private final String entityName;
    protected ADAO(Class<T> entityClass, EntityManagerFactory emf) {
        // When creating a DAO, the entity class must be specified to use in queries
        this.entityType = entityClass;
        this.entityName = entityClass.getSimpleName();
        this.emf = emf;
    }

    // Getter is used in E.G PersonDAO to get emf from super class
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    // Queries
    public T findById(Object id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(entityType, id);
        } catch (UnknownEntityException e) {
            System.out.println("Unknown entity: " + entityType.getSimpleName());
            return null;
        }
    }

    public List<T> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<T> query = em.createQuery("SELECT t FROM  "+entityName+" t", entityType);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T create(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
            return t;
        } catch (ConstraintViolationException e) {
            System.out.println("Constraint violation: " + e.getMessage());
            return null;
        }
    }

    public T update(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            T found = em.find(entityType, t.getId()); //
            if(found == null) {
                throw new EntityNotFoundException();
            }
            em.getTransaction().begin();
            T merged = em.merge(t);
            em.getTransaction().commit();
            return merged;
        } catch (ConstraintViolationException e) {
            System.out.println("Constraint violation: " + e.getMessage());
            return null;
        }
    }

    public void delete(T t) {
        try (EntityManager em = emf.createEntityManager()) {
            T found = em.find(entityType, t.getId()); //
            if(found == null) {
                throw new EntityNotFoundException();
            }
            em.getTransaction().begin();
            em.remove(found);
            em.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            e.printStackTrace();
        }
    }

    public void truncate() {
        // Remove all tuples from table
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            String sql = "TRUNCATE TABLE " + entityName + " RESTART IDENTITY CASCADE"; // CASCADE drops dependent objects
            em.createNativeQuery(sql).executeUpdate();
            em.getTransaction().commit();

            // Start sequence from 1 again
            try {
                em.getTransaction().begin();
                sql = "ALTER SEQUENCE " + entityName + "_id_seq RESTART WITH 1";
                em.createNativeQuery(sql).executeUpdate();
                em.getTransaction().commit();
            }
            catch (Exception e) {
                System.out.println("Sequence is missinga: " + entityName + "_id_seq");
                e.printStackTrace();
            }
        } catch (ConstraintViolationException e) {
            System.out.println("Constraint violation: ");
            e.printStackTrace();
        }
    }

    public void close() {
        emf.close();
    }

}