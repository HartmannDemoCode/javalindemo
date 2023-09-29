package dk.cphbusiness.rest;

import dk.cphbusiness.controllers.ContextDemoController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.path;

public class P03ContextDemo {
    private static ContextDemoController cdc = new ContextDemoController();
    public static void main(String[] args) {
        Javalin app = Javalin.create(config-> { // create method takes a Consumer<JavalinConfig> as argument to configure the server
            config.plugins.enableDevLogging();
            config.http.defaultContentType = "application/json";
            config.routing.contextPath = "/demoapp/api";
            // For wednesday:
//          config.plugins.register(new RouteOverviewPlugin("/routes")); // overview of all registered routes at /routes for api documentation
//          config.accessManager(new AccessManager() { @Override public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<? extends RouteRole> set) throws Exception { throw new UnsupportedOperationException("Not implemented yet"); } });
        }).start(7007);
        app.routes(getRequestDemoRoutes());
        app.routes(getResponseDemoRoutes());
    }
    private static EndpointGroup getRequestDemoRoutes() {
        return () -> {
            path("/context_request_demo", () -> {
                get("/", cdc.getDemo());
                // pathParam("name")                     // path parameter by name as string
                get("/pathparam/{name}", cdc.getPathParamDemo());
                // header("name")                        // request header by name (can be used with Header.HEADERNAME)
                get("/header", cdc.getHeaderDemo());
                // queryParam("name")                    // query parameter by name as string
                get("/queryparam", cdc.getQueryParamDemo());
                // req()                                 // get the underlying HttpServletRequest
                get("/req", cdc.getRequestDemo());
                // WEDNESDAY:
                // bodyasclass
                post("/bodyasclass", cdc.getBodyAsClassDemo());
                // bodyValidator
                post("/bodyvalidator", cdc.getBodyValidatorDemo());
                });
        };
    }
    private static EndpointGroup getResponseDemoRoutes() {
        // Things to do with the http result before we send it of to the client:
        return () -> {
            path("/context_response_demo", () -> {
                // contentType("type")                   // set the response content type
                get("/contenttype", cdc.getContentTypeDemo());
                //header("name", "value")               // set response header by name (can be used with Header.HEADERNAME)
                get("/header", cdc.setHeaderDemo());
                //status(code)                          // set the response status code
                get("/status", cdc.setStatusDemo());
                //json(obj)                             // calls result(jsonString), and also sets content type to json
                get("/json", cdc.setJsonDemo());
                //html("html")                          // calls result(string), and also sets content type to html
                get("/html", cdc.setHtmlDemo());
                //render("/template.tmpl", model)       // calls html(renderedTemplate)
                get("/render", cdc.setRenderDemo());
            });
        };
    }
}
