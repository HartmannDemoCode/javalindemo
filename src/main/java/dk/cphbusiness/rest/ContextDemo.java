package dk.cphbusiness.rest;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

public class ContextDemo {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7007);
        app.routes(getRoutes());
    }
    private static EndpointGroup getRoutes() {
        return () -> {
            route
        }
    }
}
