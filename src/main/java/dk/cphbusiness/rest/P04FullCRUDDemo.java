package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.IController;
import dk.cphbusiness.controllers.PersonController;

import static io.javalin.apibuilder.ApiBuilder.*;

public class P04FullCRUDDemo {
    private static IController personController = new PersonController();
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .startServer(7007)
                .setRoutes(()->{
                    path("/person", () -> {
                        get("/", personController.getAll());
                        get("/{id}", personController.getById());
                        post("/", personController.create());
                        put("/{id}", personController.update());
                        delete("/{id}", personController.delete());
                    });
                })
                .setRoutes(()->{
                    path("/test", () -> {
                        get("/", ctx->ctx.result("Hello World"));
                        get("/{id}", ctx->ctx.result("Hello World "+ctx.pathParam("id")));
                        post("/", ctx->ctx.result("Hello World "+ctx.body()));
                        put("/{id}", ctx->ctx.result("Url: "+ctx.fullUrl()+", Path parameter: "+ctx.pathParam("id")+", Body: "+ctx.body()));
                        delete("/{id}", ctx->ctx.result("Hello World "+ctx.pathParam("id")));
                    });
                });
    }
}
