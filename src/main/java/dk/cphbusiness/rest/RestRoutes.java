package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.PersonController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RestRoutes {
    PersonController personController = new PersonController();

    public EndpointGroup getPersonRoutes() {
        return () -> {
            path("/person", () -> {
                get("/", personController.getAll());
                get("/{id}", personController.getById());
                get("/name/{name}", personController.getByName());
                post("/", personController.create());
                put("/{id}", personController.update());
                delete("/{id}", personController.delete());
            });
        };
    }
}
