package dk.cphbusiness.rest;

import dk.cphbusiness.rest.controllers.IController;
import dk.cphbusiness.rest.controllers.PersonEntityController;
import dk.cphbusiness.security.SecurityRoutes;

public class P08All {
    // 1. Hashing of passwords in security.User
    // 2. Login and register in SecurityController
    // 3. Authenticate in SecurityController
    // 4. Authorize in SecurityController
    // 5. SecurityRoutes (auth and protected)
    // 6. SecurityTest with Login and token send to protected

    private static IController personController = PersonEntityController.getInstance();
    public static void main(String[] args) {
        ApplicationConfig
            .getInstance()
            .initiateServer()
            .checkSecurityRoles() // check for role when route is called
            .setRoutes(SecurityRoutes.getSecurityRoutes())
            .setRoutes(new RestRoutes().personEntityRoutes)
            .startServer(7007)
            .setCORS()
            .setGeneralExceptionHandling()
            .setErrorHandling()
            .setApiExceptionHandling();
    }

}
