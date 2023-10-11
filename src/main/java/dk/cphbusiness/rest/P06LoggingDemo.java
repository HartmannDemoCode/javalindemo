package dk.cphbusiness.rest;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P06LoggingDemo {
    // https://mkyong.com/logging/slf4j-logback-tutorial/
    // Step 1: Add dependencies to pom.xml: slf4j-api, logback-classic
    // Step 2: Add dontuselogback.xml to src/main/resources
    // Step 3: Add logger to class
    // Step 4: Use logger
    private static final Logger logger = LoggerFactory.getLogger(P06LoggingDemo.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7007);
        app.get("/hello", ctx -> {
            logger.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Hello World");
            logger.debug("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYY Hello World");
            ctx.result("Hello World");
        });
    }
}
