package dk.cphbusiness.controllers;

import dk.cphbusiness.persistence.daos.PersonDAO;
import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.dtos.PersonEntityDTO;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.persistence.model.Person;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;

public class PersonEntityController implements IController {

    PersonDAO personDAO = PersonDAO.getPersonDao(HibernateConfig.getEntityManagerFactory(false));

    @Override
    public Handler getAll() {
        return ctx -> {

        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
            ctx.pathParamAsClass("id", Integer.class)
                    .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            Person p = personDAO.findById(id);
            if (p == null)
                throw new ApiException(404, "No person with that id");
            ctx.json(p);
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<PersonEntityDTO> validator = ctx.bodyValidator(PersonEntityDTO.class);
//            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            PersonEntityDTO person = ctx.bodyAsClass(PersonEntityDTO.class);
            personDAO.create(person.getEntity());
            ctx.json(person).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            String id = (ctx.pathParam("id"));
            PersonEntityDTO person = ctx.bodyAsClass(PersonEntityDTO.class);
            person.setId(id);
            personDAO.update(person.getEntity());
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            String id = ctx.pathParam("id");
            PersonEntityDTO person = new PersonEntityDTO(personDAO.findById(Integer.parseInt(id)));
            personDAO.delete(person.getEntity());
            ctx.json(person);
        };
    }
}
