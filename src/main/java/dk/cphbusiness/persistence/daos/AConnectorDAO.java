package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.exceptions.EntityNotFoundException;
import dk.cphbusiness.persistence.model.IAssociableEntity;
import dk.cphbusiness.persistence.model.IJPAEntity;
import jakarta.persistence.EntityManagerFactory;

//interface IJPAAssociationEntity<T, A> extends IJPAEntity<T>, IAssociableEntity<A>{ } // I AM Type T and I would like to associate with type A. Combine the 2 interfaces to use below, in order to give type A both a getId() and addAssociation() method and add and remove association methods.

/**
 * Purpose of this class is to connect entities
 * Author: Thomas Hartmann
 */
abstract class AConnectorDAO<T extends IJPAEntity, A extends IAssociableEntity & IJPAEntity> implements IConnectorDAO<T, A>{
    private EntityManagerFactory emf;
    private final Class<T> entityType;
    private final Class<A> associationType;
//    private final String entityName;
    protected AConnectorDAO(Class<T> entityClass, Class<A> associationType, EntityManagerFactory emf) {
        // When creating a DAO, the entity class must be specified to use in queries
        this.entityType = entityClass;
        this.associationType = associationType;
//        this.entityName = entityClass.getSimpleName();
        this.emf = emf;
    }

    // Getter is used in E.G PersonDAO to get emf from super class
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
    public void addAssociation(T entity, A association){
        // Bind A to T (T must exist in DB, while A may not and will be persisted if not)
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            T dbEntity = em.find(entityType, entity.getId());
            if(dbEntity == null){
                throw new EntityNotFoundException("Entity with ID: "+entity.getId()+"not found");
            }
            A dbAssociation = em.find(associationType, association.getId());
            if(dbAssociation == null){
                em.persist(association);
                dbAssociation = association;
            }
            dbAssociation.addAssociation(dbEntity);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void removeAssociation(T entity, A association){
        // Unbind A from T (Both must exist in DB)
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            T dbEntity = em.find(entityType, entity.getId());
            A dbAssociation = em.find(associationType, association.getId());
            if(dbEntity == null || dbAssociation == null){
                throw new EntityNotFoundException("One or both entities not found");
            }
            dbAssociation.removeAssociation(dbEntity);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
