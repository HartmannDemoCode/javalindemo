package dk.cphbusiness.controllers;

import dk.cphbusiness.persistence.daos.PersonDAO;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.dtos.PersonDTO;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.persistence.model.Person;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;

public class PersonEntityController implements IController {

    private static PersonEntityController instance;
    private static PersonDAO personDAO;
    private PersonEntityController() { }

    public static PersonEntityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new PersonEntityController();
        }
        // Everytime we request an instance, we get a new EMF, so we can get the proper EMF for test or prod
        personDAO = new PersonDAO(Person.class, HibernateConfig.getEntityManagerFactory());
        return instance;
    }


    @Override
    public Handler getAll() {
        return ctx -> {
            personDAO.getEntityManagerFactory().getProperties().forEach((k,v) -> System.out.println(k + " : " + v));
//                throw new Exception("NOOOOOOOOOOOOOOOOOOOOO still in test mode");
//            } else {
//                throw new Exception("NOOOOOOOOOOOOOOOOOOOO NOT in test mode");
//            }
            ctx.status(HttpStatus.OK).json(PersonDTO.getEntities(personDAO.getAll()));
        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
//            ctx.pathParamAsClass("id", Integer.class)
//                    .check(id -> id > 0 && id < 4, "Id must be between 1 and 3"); // Use a path param validator
            int id = Integer.parseInt(ctx.pathParam("id"));
            Person p = personDAO.findById(id);
            if (p == null)
                throw new ApiException(404, "No person with that id");
            ctx.status(HttpStatus.OK).json(p);
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<PersonDTO> validator = ctx.bodyValidator(PersonDTO.class);
//            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            personDAO.create(person.getEntity());
            ctx.json(person).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            String id = (ctx.pathParam("id"));
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            person.setId(id);
            personDAO.update(person.getEntity());
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            String id = ctx.pathParam("id");
            PersonDTO person = new PersonDTO(personDAO.findById(Integer.parseInt(id)));
            personDAO.delete(person.getEntity());
            ctx.json(person);
        };
    }
}
