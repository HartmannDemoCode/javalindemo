package dk.cphbusiness.persistence.daos;

public interface IConnectorDAO<T, A> {
    // T is the primary entity type we want to be associated with. E.g. Address would be T and Person would be A.
    void addAssociation(T entity, A association);
    void removeAssociation(T entity, A association);

}
