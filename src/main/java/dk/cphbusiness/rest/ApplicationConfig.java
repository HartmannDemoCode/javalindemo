package dk.cphbusiness.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.ISecurityController;
import dk.cphbusiness.security.SecurityController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.plugin.bundled.RouteOverviewPlugin;
import jakarta.persistence.EntityManagerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.path;
//import io.javalin.plugin.bundled.

public class ApplicationConfig {
    private ObjectMapper jsonMapper = new ObjectMapper();
    private static ApplicationConfig appConfig;
    private static Javalin app;
    private ApplicationConfig() {
    }

    public static ApplicationConfig getInstance() {
        if (appConfig == null) {
            appConfig = new ApplicationConfig();
            initiateServer();
        }
        return appConfig;
    }

    public static ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {
            // add an accessManager. Even though it does nothing, now it is there to be updated later.
//            config.accessManager(((handler, context, set) -> {}));
            config.plugins.enableDevLogging(); // enables extensive development logging in terminal
            config.http.defaultContentType = "application/json"; // default content type for requests
            config.routing.contextPath = "/api"; // base path for all routes
            config.plugins.register(new RouteOverviewPlugin("/routes")); // html overview of all registered routes at /routes for api documentation: https://javalin.io/news/2019/08/11/javalin-3.4.1-released.html
        });
        return appConfig;
    }

    public ApplicationConfig setRoutes(EndpointGroup routes) {
        app.routes(() -> {
            path("/", routes); // e.g. /person
        });
        return appConfig;
    }

    public ApplicationConfig setCORS() {
        app.updateConfig(config -> {
            config.accessManager((handler, ctx, permittedRoles) -> {
                ctx.header("Access-Control-Allow-Origin", "*");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
                ctx.header("Access-Control-Allow-Credentials", "true");

                if (ctx.method().equals("OPTIONS"))
                    ctx.status(200).result("OK");
            });
        });
        return appConfig;
    }

    public ApplicationConfig checkSecurityRoles() {
        // Check roles on the user (ctx.attribute("username") and compare with permittedRoles using securityController.authorize()
        app.updateConfig(config -> {

            config.accessManager((handler, ctx, permittedRoles) -> {
                // permitted roles are defined in the routes: get("/", ctx -> ctx.result("Hello World"), Role.ANYONE);

                Set<String> allowedRoles = permittedRoles.stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
                if(allowedRoles.contains("ANYONE")) {
                    handler.handle(ctx);
                    return;
                }
                ISecurityController securityController = SecurityController.getInstance();

                String userName = ctx.attribute("userName");
                if(userName == null) ctx.json(jsonMapper.createObjectNode().put("msg","Not authorized. No username were added from the token")).status(401);

                System.out.println("NOW CHECKING THE USERS ROLES");
                if (securityController.authorize(userName, allowedRoles))
                    handler.handle(ctx);
                else
                    throw new ApiException(401, "Unauthorized");
            });
        });
        return appConfig;
    }


    public ApplicationConfig startServer(int port) {
        app.start(port);

        return appConfig;
    }

    public ApplicationConfig stopServer(){
        app.stop();
        return appConfig;
    }
//    public static int getPort() {
//        return Integer.parseInt(Utils.getPomProp("javalin.port"));
//    }

    public ApplicationConfig setErrorHandling() {
        // To use this one, just set ctx.status(404) in the controller and add a ctx.attribute("message", "Your message") to the ctx
        // Look at the PersonController: delete() method for an example
        app.error(404, ctx -> {
            String message = ctx.attribute("message");
            message = "{\"message\": \"" + message + "\"}";
            ctx.json(message);
        });
        return appConfig;
    }

    public ApplicationConfig setApiExceptionHandling() {
        // tested in PersonController: getAll()
        app.exception(ApiException.class, (e, ctx) -> {
            int statusCode = e.getStatusCode();
            System.out.println("Status code: " + statusCode + ", Message: " + e.getMessage());
            var on = jsonMapper
                    .createObjectNode()
                    .put("status", statusCode)
                    .put("message", e.getMessage());
            ctx.json(on);
        });
        return appConfig;
    }
    public ApplicationConfig setGeneralExceptionHandling(){
        app.exception(Exception.class, (e,ctx)->{
            e.printStackTrace();
            ctx.result(e.getMessage());
        });
        return appConfig;
    }

}
