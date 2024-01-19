package dk.cphbusiness.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.cphbusiness.dtos.SimplePersonDTO;
import io.javalin.http.Handler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

/**
 * Purpose: To demonstrate the use of the Context object
 * Author: Thomas Hartmann
 */
public class ContextDemoController {
    private ObjectMapper jsonMapper = new ObjectMapper();
    public Handler getDemo(){
        return ctx -> {
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("message", "Hello World");
            ctx.json(json);
        };
    }
    public Handler getPathParamDemo(){
        return ctx -> {
            String name = ctx.pathParam("name");
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("name", name);
            json.put("message","GetPathParamDemo");
            ctx.json(json);
        };
    }
    public Handler getHeaderDemo(){
        return ctx -> {
            String header = ctx.header("X-EXAMPLE-HEADER");
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("header", header);
            json.put("message","GetHeaderDemo");
            ctx.json(json);
        };
    }
    public Handler getQueryParamDemo(){
        return ctx -> {
            String queryParam = ctx.queryParam("qp");
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("queryParam", queryParam);
            json.put("message","GetQueryParamDemo");
            ctx.json(json);
        };
    }
    public Handler getRequestDemo(){
        return ctx -> {
            HttpServletRequest request = ctx.req();
            System.out.println("REQUEST: "+request);
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("message","GetRequestDemo");
            ctx.json(json);
        };
    }

    public Handler getBodyAsClassDemo(){
        return ctx -> {
            SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("person", jsonMapper.valueToTree(person));
            json.put("message","GetBodyAsClassDemo");
            ctx.json(person);
        };
    }
    public Handler getBodyValidatorDemo(){
        return ctx -> {
            SimplePersonDTO person = ctx.bodyValidator(SimplePersonDTO.class)
                    .check(p -> p.getLastName() != null && p.getLastName().length() > 0, "Name cannot be null or empty")
                    .check(p -> p.getBirthday().isBefore(LocalDate.now()) , "Age must be a positive number")
                    .get();
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("person", json);
            json.put("message","GetBodyValidatorDemo");
            ctx.json(person);
        };
    }

    // RESPONSE DEMO
    public Handler getContentTypeDemo(){
        return ctx -> {
            HttpServletResponse response = ctx.res();
            response.setContentType("application/json");
            System.out.println("CONTENT TYPE: "+response.getContentType());
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("contentType", response.getContentType());
            json.put("message","GetContentTypeDemo");
            ctx.json(json);
        };
    }
    public Handler setHeaderDemo(){
        return ctx -> {
            HttpServletResponse response = ctx.res();
            response.setHeader("X-EXAMPLE-HEADER", "Hello World");
            System.out.println(response.getHeader("X-EXAMPLE-HEADER"));
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("message","SetHeaderDemo");
            ctx.json(json);
        };
    }
    public Handler setStatusDemo(){
        return ctx -> {
            HttpServletResponse response = ctx.res();
            response.setStatus(418);
            System.out.println(response.getStatus());
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("message","SetStatusDemo");
            ctx.json(json);
        };
    }
    public Handler setJsonDemo(){
        return ctx -> {
            ObjectNode json = jsonMapper.createObjectNode();
            json.put("message","SetJsonDemo");
            ctx.json(json);
        };
    }
    public Handler setHtmlDemo(){
        return ctx -> {
            ctx.html("<html><head><title>Hello World Page</title><style>body {background-color:black; color:white;}</style></head><body><h1>Hello to you World</h1><img src=\"https://images.pexels.com/photos/87651/earth-blue-planet-globe-planet-87651.jpeg\" width=\"1000\"></body></html>");
        };
    }
    public Handler setRenderDemo(){
        return ctx -> {
            // This file must be in folder /src/main/jte and 2 maven dependencies are necessary: jte and javalin-rendering. See pom.xml
            ctx.render("template.jte");
        };
    }
}
