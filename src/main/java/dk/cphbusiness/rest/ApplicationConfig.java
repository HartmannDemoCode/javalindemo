package dk.cphbusiness.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.security.ISecurityController;
import dk.cphbusiness.security.SecurityController;
import dk.cphbusiness.security.dtos.UserDTO;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.RouteOverviewPlugin;
import jakarta.persistence.EntityManagerFactory;

import java.util.Objects;
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
        }
        return appConfig;
    }

    public ApplicationConfig initiateServer() {
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
        app.before(ctx -> {
            setCorsHeaders(ctx);
        });
        app.options("/*", ctx -> {
            setCorsHeaders(ctx);
        });
        return appConfig;
    }

    private static void setCorsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    public ApplicationConfig checkSecurityRoles() {
        // Check roles on the user (ctx.attribute("username") and compare with permittedRoles using securityController.authorize()
        app.updateConfig(config -> {

            config.accessManager((handler, ctx, permittedRoles) -> {
                // permitted roles are defined in the routes: get("/", ctx -> ctx.result("Hello World"), Role.ANYONE);

                Set<String> allowedRoles = permittedRoles.stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
                if(allowedRoles.contains("ANYONE") || Objects.equals(ctx.method(), "OPTIONS")) {
                    handler.handle(ctx);
                    return;
                }

                UserDTO user = ctx.attribute("user");
                System.out.println("USER FROM AUTHENTICATE: "+user);
                if(user == null)
                    ctx.status(HttpStatus.UNAUTHORIZED)
                            .json(jsonMapper.createObjectNode()
                                    .put("msg","Not authorized. No username were added from the token"));

                if (SecurityController.getInstance().authorize(user, allowedRoles))
                    handler.handle(ctx);
                else
                    throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized"+allowedRoles);
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
            String message = ctx.attribute("msg");
            message = "{\"msg\": \"" + message + "\"}";
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
                    .put("msg", e.getMessage());
            ctx.json(on);
            ctx.status(statusCode);
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
