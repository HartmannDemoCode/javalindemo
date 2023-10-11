package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.dtos.PersonDTO;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.Role;
import dk.cphbusiness.security.SecurityRoutes;
import dk.cphbusiness.security.User;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static ApplicationConfig appConfig;
    private static EntityManagerFactory emfTest;
    private static final String BASE_URL = "http://localhost:7777/api";
    private static User user, admin, superUser;
    private static Role userRole, adminRole;

    @BeforeAll
    static void setUpAll() {
        RestRoutes restRoutes = new RestRoutes();

        // Setup test database using docker testcontainers
        HibernateConfig.setTestMode(true);
        emfTest = HibernateConfig.getEntityManagerFactory();

        // Start server
        appConfig = ApplicationConfig.getInstance().startServer(7777)
                .setErrorHandling()
                .setGeneralExceptionHandling()
                .setRoutes(restRoutes.getPersonRoutes())
                .setRoutes(SecurityRoutes.getSecurityRoutes())
                .setRoutes(SecurityRoutes.getSecuredRoutes())
                .setApiExceptionHandling()
        ;

    }

    @AfterAll
    static void afterAll() {
        HibernateConfig.setTestMode(false);
        appConfig.stopServer();
        emfTest.close();
    }

    @BeforeEach
    void setUpEach() {
        // Setup test database for each test
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User u").executeUpdate();
            em.createQuery("DELETE FROM Role r").executeUpdate();
            user = new User("user", "user123");
            admin = new User("admin", "admin123");
            superUser = new User("super", "super123");
            userRole = new Role("user");
            adminRole = new Role("admin");
            user.addRole(userRole);
            admin.addRole(adminRole);
            superUser.addRole(userRole);
            superUser.addRole(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(superUser);
            em.persist(userRole);
            em.persist(adminRole);
            em.getTransaction().commit();
        }
    }

    @Test
    @DisplayName("Hul igennem")
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get(BASE_URL+"/person").peek().then().statusCode(200);
    }

    @Test
    @DisplayName("Get person 1")
    void getOne() {

        given()
//                .header("Authorization", adminToken)
                .contentType("application/json")
//                .body(GSON.toJson(person))
                .when()
                .get(BASE_URL + "/person/1")
                .then()
                .assertThat()
                .statusCode(200)
                // then
                .body("name", equalTo("Kurt"))
                .body("age", equalTo(23));
    }
    @Test
    @DisplayName("Get All Persons")
    void getAll() {

        given()
//                .header("Authorization", adminToken)
                .contentType("application/json")
//                .body(GSON.toJson(person))
                .when()
                .get(BASE_URL + "/person")
                .then()
                .assertThat()
                .statusCode(200)
                // then
                .body("size()", equalTo(3))
                .body("1.name", equalTo("Kurt"));
    }
    @Test
    @DisplayName("Get all persons check first person")
    void testAllBody(){
        // given, when, then
        given()
                .when()
                .get(BASE_URL + "/person")
                .prettyPeek()
                .then()
                .body("1.firstName", is("Hans"));
    }
    @Test
    @DisplayName("Get all persons 2")
    void testAllBody2(){
        // given, when, then
        given()
//                .log().all()
                .when()
                .get(BASE_URL + "/person")
                .then()
                .log().body()
//                .body("[0].firstName", is("Hans"));
                .body("$", hasItems(hasEntry("firstName","Peter")));

        ;
    }

    @Test
    @DisplayName("Json PATH and DTOs")
    void testAllBody3(){
        Response response = given()
                .when()
                .get(BASE_URL + "/person");
        JsonPath jsonPath = response.jsonPath();

        // Get the map of persons from the outer json object
        Map<String, Map<String, Object>> personsMap = jsonPath.getMap("$");

        // Convert the map of persons to an array of PersonDTO
        PersonDTO[] persons = personsMap.values()
                .stream()
                .map(personData -> new PersonDTO(
                        (String) personData.get("name"),
                        (int) personData.get("age")
                ))
                .toArray(PersonDTO[]::new);

        assertTrue(persons.length == 3);
    }
}
