package dk.cphbusiness.rest;

import dk.cphbusiness.rest.controllers.IController;
import dk.cphbusiness.rest.controllers.PersonController;

import static io.javalin.apibuilder.ApiBuilder.*;

public class P04FullCRUDDemo {
    private static IController personController = new PersonController();
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoutes(new RestRoutes().getOpenRoutes())
                .setCORS()
                .setRoutes(()->{
                    path("/test", () -> {
                        get("/", ctx->ctx.contentType("text/plain").result("Hello World"));
                        get("/{id}", ctx->ctx.result("Hello World "+ctx.pathParam("id")));
//                        post("/", ctx->ctx.result("Hello World "+ctx.body()));
//                        put("/{id}", ctx->ctx.result("Url: "+ctx.fullUrl()+", Path parameter: "+ctx.pathParam("id")+", Body: "+ctx.body()));
//                        delete("/{id}", ctx->ctx.result("Hello World "+ctx.pathParam("id")));
                    });
                });
    }
}
