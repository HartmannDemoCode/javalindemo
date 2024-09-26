package dk.cphbusiness.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.persistence.daos.ConnectorDAO;
import dk.cphbusiness.persistence.daos.PersonDAO;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.dtos.PersonDTO;
import dk.cphbusiness.exceptions.ApiException;
import dk.cphbusiness.persistence.model.*;
import dk.cphbusiness.rest.Populator;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;

/**
 * Purpose: To demonstrate rest api with Javalin and a database.
 * Author: Thomas Hartmann
 */
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
            if (p == null){
                ctx.attribute("msg", "No person with that id");
                throw new ApiException(404, "No person with that id");
            }
            ctx.status(HttpStatus.OK).json(new PersonDTO(p));
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<PersonDTO> validator = ctx.bodyValidator(PersonDTO.class);
//            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            Person created = personDAO.create(person.toEntity());
            ctx.json(new PersonDTO(created)).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            String id = (ctx.pathParam("id"));
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            person.setId(id);
            personDAO.update(person.toEntity());
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            String id = ctx.pathParam("id");
            Person found = personDAO.findById(Integer.parseInt(id));
            if(found == null)
                throw new ApiException(404, "No person with that id");
            PersonDTO person = new PersonDTO(found);
            personDAO.delete(person.toEntity());
            ctx.json(person);
        };
    }
    public Handler connectPersonToAddress(){
        return ctx -> {
            String personId = ctx.pathParam("personId");
            String addressId = ctx.pathParam("addressId");
            Person person = personDAO.findById(Integer.parseInt(personId));
            Address address = personDAO.getAddressById(Integer.parseInt(addressId));
            new ConnectorDAO(Person.class, Address.class, HibernateConfig.getEntityManagerFactory()).addAssociation(person, address);
            ctx.json(personDAO.findById(person.getId()));
        };
    }
    public Handler connectPersonToPhone(){
        return ctx -> {
            String personId = ctx.pathParam("personId");
            String phoneId = ctx.pathParam("phoneId");
            Person person = personDAO.findById(Integer.parseInt(personId));
            Phone phone = personDAO.getPhoneById(phoneId);
            new ConnectorDAO(Person.class, Phone.class, HibernateConfig.getEntityManagerFactory()).addAssociation(person, phone);
            ctx.json(personDAO.findById(person.getId()));
        };
    }
    public Handler connectPersonToHobby(){
        return ctx -> {
            String personId = ctx.pathParam("personId");
            String hobbyId = ctx.pathParam("hobbyId");
            Person person = personDAO.findById(Integer.parseInt(personId));
            Hobby hobby = personDAO.getHobbyById(hobbyId);
            new ConnectorDAO(Person.class, Hobby.class, HibernateConfig.getEntityManagerFactory()).addAssociation(person, hobby);
            ctx.json(personDAO.findById(person.getId()));
        };
    }
    public Handler resetData(){
        return ctx -> {
            new Populator().createUsersAndRoles(HibernateConfig.getEntityManagerFactory());
            new Populator().createPersonEntities(HibernateConfig.getEntityManagerFactory());
            ctx.json(new ObjectMapper().createObjectNode().put("message", "Data reset"));
        };
    }
}
