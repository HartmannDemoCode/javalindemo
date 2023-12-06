package dk.cphbusiness.persistence.daos;

import dk.cphbusiness.persistence.model.IAssociableEntity;
import dk.cphbusiness.persistence.model.IJPAEntity;
import jakarta.persistence.EntityManagerFactory;
//import dk.cphbusiness.persistence.dao.IJPAAssociationEntity;

public class ConnectorDAO<T extends IJPAEntity,A extends IAssociableEntity & IJPAEntity> extends AConnectorDAO<T,A>{
    // Use this class for connecting entities
    public ConnectorDAO(Class entityClass, Class associationType, EntityManagerFactory emf) {
        super(entityClass, associationType, emf);
    }
}
