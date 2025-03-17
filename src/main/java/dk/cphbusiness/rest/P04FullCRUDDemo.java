package dk.cphbusiness.rest;

import dk.cphbusiness.rest.controllers.IController;
import dk.cphbusiness.rest.controllers.PersonController;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To demonstrate the use of unprotected routes
 * Author: Thomas Hartmann
 */
public class P04FullCRUDDemo {
    private static IController personController = new PersonController();
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoute(new RestRoutes().getOpenRoutes())
                .setCORS()
                .setRoute(()->{
                    path("/test", () -> {
                        get("/", ctx->ctx.contentType("text/plain").result("Hello World"));
                        get("/{id}", ctx->ctx.contentType("text/plain").result("Hello World "+ctx.pathParam("id")));
                    });
                });
    }
}
