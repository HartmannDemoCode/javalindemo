package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.persistence.model.IJPAEntity;
import jakarta.persistence.EntityManagerFactory;

/**
 * This class is a generic DAO (Data Access Object) that can be used to perform CRUD operations on any entity.
 * @param <T> The entity class that the DAO should be used for.
 */
public class DAO<T extends IJPAEntity> extends ADAO<T> {

    public DAO(Class<T> entityClass, EntityManagerFactory emf) {
        super(entityClass, emf);
    }


}
