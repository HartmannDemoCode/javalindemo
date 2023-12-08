package dk.cphbusiness.controllers;

import dk.cphbusiness.dtos.SimplePersonDTO;
import dk.cphbusiness.exceptions.ApiException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;

import java.util.HashMap;
import java.util.Map;

public class PersonController implements IController {
    Map<Integer, SimplePersonDTO> persons = new HashMap(Map.of(
        1, new SimplePersonDTO("Kurt", 23),
        2, new SimplePersonDTO("Hanne", 21),
        3, new SimplePersonDTO("Tina", 25),
        4, new SimplePersonDTO("Carla", 44),
        5, new SimplePersonDTO("Hans", 75)
    ));

    @Override
    public Handler getAll() {
        boolean isExceptionTest = false;
        return ctx -> {
            if (isExceptionTest) {
                throw new ApiException(500, "Something went wrong in the getAll method in the PersonController");
            }
            ctx.json(persons);
        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
            ctx.pathParamAsClass("id", Integer.class)
            .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            if(!persons.containsKey(id))
                throw new ApiException(404, "No person with that id");
            ctx.json(persons.get(id));
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<SimplePersonDTO> validator = ctx.bodyValidator(SimplePersonDTO.class);
            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
            ctx.json(person).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            ctx
                    .pathParamAsClass("id", Integer.class) // returns a validator
                    .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
            this.persons.put(id, person);
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            if(! persons.containsKey(id)){
                ctx.status(404);
                ctx.attribute("msg", String.format("No person with id: {id}", id));
                return;
            }
            SimplePersonDTO person = this.persons.remove(id);
            ctx.json(person);
        };
    }

    public Handler getByName() {
        return ctx -> {
            try {
                SimplePersonDTO found = persons
                        .values()
                        .stream()
                        .filter((person) -> person.getName().equals(ctx.pathParam("name")))
                        .toList()
                        .get(0);
                ctx.json(found);
            } catch (IndexOutOfBoundsException e) {
                throw new ApiException(404, String.format("No person with name: %s", ctx.pathParam("name")));
            }
        };
    }
}
