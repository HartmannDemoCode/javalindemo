package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.persistence.model.IJPAEntity;

import java.util.List;
import java.util.Set;

/**
 * Purpose: This is an interface for making a DAO (Data Access Object) that can be used to perform CRUD operations on any entity.
 * Author: Thomas Hartmann
 * @param <T>
 */
interface IDAO<T extends IJPAEntity> {


//    void setEntityManagerFactory(EntityManagerFactory emf);

//    EntityManagerFactory getEntityManagerFactory(); // used for getting emf from super class

    T findById(Object id);

    Set<T> getAll();

    T create(T t);

    T update(T t);

    void delete(T t);

    void truncate();

    void close();
}