package dk.cphbusiness.persistence.model;

public interface IAssociableEntity<T> { // T is the primary entity type we want to be associated with. E.g. Address would implelnt this with T as Person.
    void addAssociation(T entity);
    void removeAssociation(T entity);
}
