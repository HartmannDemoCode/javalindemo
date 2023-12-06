package dk.cphbusiness.exceptions;

import jakarta.persistence.EntityManagerFactory;

public class EntityNotFoundException extends Exception {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
