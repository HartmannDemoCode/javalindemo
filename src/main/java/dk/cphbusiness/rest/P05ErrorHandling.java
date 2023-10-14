package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.PersonController;
import dk.cphbusiness.data.HibernateConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class P05ErrorHandling {
    // ApplicationConfig.setApiExceptionHandling() is called in the ApplicationConfig constructor
    //
    private static PersonController personController = new PersonController();
    private static RestRoutes restRoutes = new RestRoutes();

    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoutes(restRoutes.getPersonRoutes())
                .setRoutes(() -> {
                    path("/test", () -> {
                        get("/", ctx -> ctx.result("Hello World"));
                        get("/{id}", ctx -> ctx.result("Hello World " + ctx.pathParam("id")));
                        post("/", ctx -> ctx.result("Hello World " + ctx.body()));
                        put("/{id}", ctx -> ctx.result("Url: " + ctx.fullUrl() + ", Path parameter: " + ctx.pathParam("id") + ", Body: " + ctx.body()));
                        delete("/{id}", ctx -> ctx.result("Hello World " + ctx.pathParam("id")));
                    });
                })
                .setErrorHandling()
                .setApiExceptionHandling();
    }
}
