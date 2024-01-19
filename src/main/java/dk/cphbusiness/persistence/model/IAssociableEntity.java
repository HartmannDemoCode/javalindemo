package dk.cphbusiness.persistence.model;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
public interface IAssociableEntity<T> { // T is the primary entity type we want to be associated with. E.g. Address would implelnt this with T as Person.
    void addAssociation(T entity);
    void removeAssociation(T entity);
}
