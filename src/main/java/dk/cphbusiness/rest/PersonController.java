package dk.cphbusiness.rest;

import dk.cphbusiness.dtos.PersonDTO;
import io.javalin.http.Handler;
import io.javalin.validation.BodyValidator;

import java.util.Map;

public class PersonController implements IController{
    Map<Integer, PersonDTO> persons = Map.of(
            1, new PersonDTO("Kurt", 23),
            2, new PersonDTO("Hanne", 21),
            3, new PersonDTO("Tina", 25)
    );
    @Override
    public Handler getAll() {
        return ctx -> {
            ctx.json(persons);
        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
            ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(persons.get(id));
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<PersonDTO> validator = ctx.bodyValidator(PersonDTO.class);
            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            ctx.json(person);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            ctx.pathParamAsClass("id", Integer.class)
                    .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            this.persons.put(id, person);
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            PersonDTO person = this.persons.remove(id);
            ctx.json(person);
        };
    }
}
