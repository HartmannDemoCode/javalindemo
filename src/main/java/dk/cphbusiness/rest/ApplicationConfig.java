package dk.cphbusiness.rest;

//import dk.cphbusiness.dtos.UserDTO;
//import dk.cphbusiness.errorHandling.ApiException;
//import dk.cphbusiness.rest.controllers.SecurityController;
//import dk.cphbusiness.utils.Utils;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.plugin.bundled.RouteOverviewPlugin;

import static io.javalin.apibuilder.ApiBuilder.path;
//import io.javalin.plugin.bundled.

public class ApplicationConfig {
    private static ApplicationConfig appConfig;
    private Javalin app;
    private ApplicationConfig() {
    }
    public static ApplicationConfig getInstance() {
        if(appConfig == null) {
            appConfig = new ApplicationConfig();
        }
        return appConfig;
    }
    public ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {
            config.plugins.enableDevLogging(); // enables extensive development logging in terminal
            config.http.defaultContentType = "application/json"; // default content type for requests
            config.routing.contextPath = "/api"; // base path for all routes
            config.plugins.register(new RouteOverviewPlugin("/routes")); // html overview of all registered routes at /routes for api documentation: https://javalin.io/news/2019/08/11/javalin-3.4.1-released.html
        });
        return appConfig;
    }

    public Javalin setRoutes(Javalin app, EndpointGroup routes) {
        app.routes(()-> {
            path("/", routes); // e.g. /person
        });
        return app;
    }

    public Javalin setCORS(Javalin app) {
        app.updateConfig(config-> {
            config.accessManager((handler, ctx, permittedRoles) -> {
                ctx.header("Access-Control-Allow-Origin", "*");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
                ctx.header("Access-Control-Allow-Credentials", "true");

                if (ctx.method().equals("OPTIONS"))
                    ctx.status(200).result("OK");
            });
        });
        return app;
    }

    public Javalin setSecurityRoles(Javalin app) {
        app.updateConfig(config -> {
                config.accessManager((handler, ctx, permittedRoles) -> {
            // Authorize the user based on the roles they have
//            SecurityController securityController = SecurityController.getController();
//            UserDTO user = ctx.attribute("user");
//            if (securityController.authorize(user, permittedRoles))
//                handler.handle(ctx);
//            else
//                throw new ApiException(401, "Unauthorized");
            });
        });
        return app;
    }


    public Javalin startServer(int port) {

        app.start(port);
        return app;
    }

//    public static int getPort() {
//        return Integer.parseInt(Utils.getPomProp("javalin.port"));
//    }
}
