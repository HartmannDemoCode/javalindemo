package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.IController;
import dk.cphbusiness.controllers.PersonController;
import dk.cphbusiness.data.HibernateConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class P04FullCRUDDemo {
    private static IController personController = new PersonController();
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoutes(new RestRoutes().getPersonRoutes())
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
