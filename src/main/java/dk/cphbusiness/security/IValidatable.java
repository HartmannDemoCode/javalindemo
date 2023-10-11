package dk.cphbusiness.security;

public interface IValidatable<T> {
    boolean validateId(T id); // T is the type of the id
}
