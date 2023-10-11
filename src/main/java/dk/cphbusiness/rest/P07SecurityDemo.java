package dk.cphbusiness.rest;

import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.security.SecurityRoutes;

public class P07SecurityDemo {
    // 1. Hashing of passwords in security.User
    // 2. Login and register in SecurityController
    // 3. Authenticate in SecurityController
    // 4. Authorize in SecurityController
    // 5. SecurityRoutes (auth and protected)
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .setRoutes(SecurityRoutes.getSecurityRoutes())
                .setRoutes(SecurityRoutes.getSecuredRoutes())
                .startServer(7007)
                .checkSecurityRoles()
                .setGeneralExceptionHandling()
                .setErrorHandling()
                .setApiExceptionHandling();
    }
}
