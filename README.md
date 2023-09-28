# Javalin code example
Javalin is a lightweight web framework for Java and Kotlin, created by the developers of the original Spark framework for Java.
It is well suited for microservice applications and/or for REST APIs.

## Getting started
To get started with Javalin, you need to add the following dependency to your pom.xml file:
```xml
<dependency>
    <groupId>io.javalin</groupId>
    <artifactId>javalin-bundle</artifactId>
    <version>5.5.0</version>
</dependency>
```

## Hello World
The following code snippet shows a simple "Hello World" example:
```java
import io.javalin.Javalin;

public class HelloWorld {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> ctx.result("Hello World"));
    }
}
```

## REST API example
The following code snippet shows a simple REST API example:
```java
import io.javalin.Javalin;

public class RestApiExample {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/hello", ctx -> ctx.result("Hello World"));
        app.get("/hello/:name", ctx -> ctx.result("Hello " + ctx.pathParam("name")));
    }
}
```

## Maven Build
To build a Jar file with Maven, First set up the maven-shade-plugin in your pom.xml file. It enables us to build an Uber JAR, also known as a Fat JAR, including all the necessary dependencies bundled inside the JAR file:
```xml
            <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <transformers>
            <transformer
                    implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>dk/cphbusiness/Main
                </mainClass> <!-- Here you should put the main class of your application -->
            </transformer>
        </transformers>
        <filters>
            <filter> <!-- This filter is needed to avoid a bug in the shade plugin -->
                <artifact>*:*</artifact>
                <excludes>
                    <exclude>module-info.class</exclude>
                    <exclude>META-INF/*.SF</exclude>
                    <exclude>META-INF/*.DSA</exclude>
                    <exclude>META-INF/*.RSA</exclude>
                </excludes>
            </filter>
        </filters>
        <relocations>
            <relocation>
                <pattern>com.example</pattern>
                <shadedPattern>my.project.com.example</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
And remember to replace the `com.example.MainClass` with the name of your main class.
Now build the project and run the jar file:
```bash
java -jar target/javlindemo.0-SNAPSHOT.jar
```

## More Javalin examples
When building a REST API we need:
1. A http method (GET, POST, PUT, DELETE, PATCH)
2. A path (e.g. /hello) used to hit the ressource from the client application
3. A response (e.g. "Hello World") or more useful some JSON representation of data.
4. A status code (e.g. 200 OK, 404 Not Found, 500 Internal Server Error)
5. A content type (e.g. text/plain, application/json, text/html)

### Handlers
Building a REST API in Javalin is done by creating handlers. A handler is a function that takes a context object as input and returns nothing. 
The context object contains information about the request and response. 
A javalin handler needs:
1. A verb (http method: GET, POST, PUT, DELETE or PATCH)
2. A path (e.g. /hello) used to hit the ressource from the client application
3. A handler function (e.g. ctx -> ctx.result("Hello World")) that takes a context object as input and returns nothing. The response is created by calling the result method on the context object.

Example:
```java
app.get("/hello", ctx -> ctx.result("Hello World"));
```

#### The most common handlers are:
**Endpoint handlers:**
1. `app.get("/ressourceName", ctx -> {})` - Runs on GET requests to /hello
2. `app.post("/ressourceName", ctx -> {})` - Runs on POST requests to /hello
3. `app.put("/ressourceName", ctx -> {})` - Runs on PUT requests to /hello
4. `app.delete("/ressourceName", ctx -> {})` - Runs on DELETE requests to /hello
5. `app.patch("/ressourceName", ctx -> {})` - Runs on PATCH requests to /hello
and also **Before/After handlers:**
6. `app.before(ctx -> {})` - Runs before all requests. Good for adding CORS headers and other global stuff.
7. `app.after(ctx -> {})` - Runs after all requests. Good 
If we add multiple before/after handlers for the same path, they will be executed in the order they were added.
This can be useful for adding authentication, caching, logging, etc.

### Context
The context object contains information about the request and response.
The context object has a lot of methods. The following are the most common:

#### Request Methods
1. `body()                                // request body as string`
2. `pathParam("name")                     // path parameter by name as string`
3. `contentType()                         // request content type`
4. `header("name")                        // request header by name (can be used with Header.HEADERNAME)`
5. `queryParam("name")                    // query param by name as string`
6. `formParams("name")                    // list of form parameters by name`
7. `formParamMap()                        // map of all form parameters`
8. `req()                                 // get the underlying HttpServletRequest`
To see more request methods, see the [Javalin documentation](https://javalin.io/documentation#context).

#### Response Methods
1. `result("result")                      // set result stream to specified string (overwrites any previously set result)       `
2. `contentType("type")                   // set the response content type`
3. `header("name", "value")               // set response header by name (can be used with Header.HEADERNAME)`
4. `status(200)                           // set response status code`
5. `json(obj)                             // calls result(jsonString), and also sets content type to json`
6. `html("html")                          // calls result(string), and also sets content type to html`
7. `render("/template.tmpl", model)       // calls html(renderedTemplate)`



1. `ctx.result("Hello World")` - Sets the response body to "Hello World"
2. `ctx.status(200)` - Sets the response status code to 200
3. `ctx.contentType("application/json")` - Sets the response content type to application/json
5. `ctx.pathParam("name")` - Gets the path parameter with the name "name"
6. `ctx.queryParam("name")` - Gets the query parameter with the name "name"

### Routes and Path (Handler groups)
To group endpoints, you can use the routes method on the Javalin object:
```java
app.routes(() -> {
    path("/users", () -> {
        get(UserController::getAllUsers);
        post(UserController::createUser);
        path("{id}", () -> {
            get(UserController::getUser);
            patch(UserController::updateUser);
            delete(UserController::deleteUser);
        });
        ws("events", UserController::webSocketEvents);
    });
});
```
(Note that path() **prefixes your paths with `/`** (if you don’t add it yourself)).
This is useful for grouping endpoints that are related to each other.

### Crud Handler
See https://javalin.io/documentation#crudhandler for information about how to implement the 5 common CRUD operations in one line of code: `app.routes(() -> { crud("users/{user-id}", new UserController()); });`

### Validators
See https://javalin.io/documentation#validators for information about how to validate request bodies and query parameters.

### Exception Mapping
See https://javalin.io/documentation#exception-mapping for information about how to map exceptions to status codes and error messages.


### Static files
To serve static files, you can use the staticFiles method on the Javalin object:
```java
app.staticFiles.location("/public");
```

# repo auto created
