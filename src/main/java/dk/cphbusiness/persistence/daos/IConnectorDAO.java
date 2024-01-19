package dk.cphbusiness.persistence.daos;

/**
* Purpose of this class is to list the methods that should be implemented in the ConnectorDAO
 * addAssociation is used to add an association between two entities (E.G. Address knows person)
 * Author: Thomas Hartmann
 */
public interface IConnectorDAO<T, A> {
    // T is the primary entity type we want to be associated with. E.g. Address would be T and Person would be A.
    void addAssociation(T entity, A association);
    void removeAssociation(T entity, A association);

}
