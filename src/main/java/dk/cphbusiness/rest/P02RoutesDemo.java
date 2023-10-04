package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.PersonController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.*;

public class P02RoutesDemo {
    public static void main(String[] args) {
        // A more complex web server using routes and endpoint groups
        Javalin app = Javalin.create()
                .start(7007);
        app.routes(getPersonRessource());
    }

    private static EndpointGroup getPersonRessource() {
        PersonController personController = new PersonController();
        return () -> {
            path("/person", () -> {
                get("/",personController.getAll());
                get("/{id}",personController.getById());
//                TODO: Covered next time on REST:
//                post("/",personController.create());
//                put("/{id}",personController.update());
//                delete("/{id}",personController.delete());
            });
        };
    }
    private static EndpointGroup alternative(){
        PersonController pc = new PersonController();
        return () -> {
            path("/person", () -> {
                get("/", pc.getAll());
//                post("/", pc.create());
                path("/{id}", () -> {
                    get("/", pc.getById());
                    get("/name/{name}", pc.getByName());
//                    put("/", pc.update());
//                    delete("/", pc.delete());
                });
            });
        };
    }
}
