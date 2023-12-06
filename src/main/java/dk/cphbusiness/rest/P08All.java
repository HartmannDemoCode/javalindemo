package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.IController;
import dk.cphbusiness.controllers.PersonController;
import dk.cphbusiness.controllers.PersonEntityController;
import dk.cphbusiness.security.SecurityController;
import dk.cphbusiness.security.SecurityRoutes;

import static io.javalin.apibuilder.ApiBuilder.*;
import static dk.cphbusiness.security.SecurityRoutes.Role;

public class P08All {
    // 1. Hashing of passwords in security.User
    // 2. Login and register in SecurityController
    // 3. Authenticate in SecurityController
    // 4. Authorize in SecurityController
    // 5. SecurityRoutes (auth and protected)
    // 6. SecurityTest with Login and token send to protected

    private static SecurityController securityController = SecurityController.getInstance();
    private static IController personController = new PersonEntityController();
    public static void main(String[] args) {
        ApplicationConfig
            .getInstance()
            .initiateServer()
            .checkSecurityRoles() // check for role when route is called
            .setRoutes(SecurityRoutes.getSecurityRoutes())
            .setRoutes(() -> {
                path("/person2", () -> {
                    get("/", ctx -> personController.getAll(), Role.ANYONE);
                    get("/{id}", ctx -> personController.getById(), Role.ANYONE);
                });
            })
                .setRoutes(() -> {
                    path("/person", () -> {
                        before("person",securityController.authenticate());
                        post("/", ctx -> personController.create(), Role.USER);
                        put("/{id}", ctx -> personController.update());
                        delete("/{id}", ctx -> personController.delete());
                    });
                })
            .startServer(7007)
            .setCORS()
            .setGeneralExceptionHandling()
            .setErrorHandling()
            .setApiExceptionHandling();
    }

}
