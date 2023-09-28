package dk.cphbusiness.rest;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RoutesDemo {
    public static void main(String[] args) {
        // A more complex web server using routes and endpoint groups
        Javalin app = Javalin.create().start(7007);
        app.routes(getPersonRessource());
    }

    private static EndpointGroup getPersonRessource() {
        PersonController personController = new PersonController();
        return () -> {
            path("/person", () -> {
                get("/",personController.getAll());
                get("/{id}",personController.getById());
                post("/",personController.create());
                put("/{id}",personController.update());
                delete("/{id}",personController.delete());
            });
        };
    }
}
