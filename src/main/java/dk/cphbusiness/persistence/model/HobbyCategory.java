package dk.cphbusiness.persistence.model;

import lombok.Getter;

@Getter
public enum HobbyCategory {
    EDUCATIONAL("Educational"),
    GENERAL("General"),
    COLLECTION_HOBBY("Collecting stuff"),
    COMPETITION("Competition");

    private final String name;

    HobbyCategory(String name) {
        this.name = name;
    }
}