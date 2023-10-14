package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.dtos.PersonDTO;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.Role;
import dk.cphbusiness.security.SecurityRoutes;
import dk.cphbusiness.security.User;
import io.restassured.RestAssured;
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
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static ApplicationConfig appConfig;
    private static final String BASE_URL = "http://localhost:7777/api";


    @BeforeAll
    static void setUpAll() {
        RestRoutes restRoutes = new RestRoutes();
        RestAssured.baseURI = BASE_URL;

        // Start server
        appConfig = ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7777)
                .setErrorHandling()
                .setGeneralExceptionHandling()
                .setRoutes(restRoutes.getPersonRoutes())
                .setRoutes(SecurityRoutes.getSecurityRoutes())
                .setRoutes(SecurityRoutes.getSecuredRoutes())
                .setApiExceptionHandling();
    }

    @AfterAll
    static void afterAll() {
        HibernateConfig.setTestMode(false);
        appConfig.stopServer();
    }

    @BeforeEach
    void setUpEach() {
    }

    @Test
    @DisplayName("Hul igennem")
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/person").peek().then().statusCode(200);
    }

    @Test
    @DisplayName("Get person 1")
    void getOne() {

        given()
//                .header("Authorization", adminToken)
                .contentType("application/json")
                .when()
                .get("/person/1")
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Kurt"))
                .body("age", equalTo(23));
    }
    @Test
    @DisplayName("Get All Persons")
    void getAll() {

        given()
                .contentType("application/json")
                .when()
                .get("/person")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("1.name", equalTo("Kurt"));
    }
    @Test
    @DisplayName("Get all persons check first person")
    void testAllBody(){
        // given, when, then
        given()
                .when()
                .get("/person")
                .prettyPeek()
                .then()
                .body("1.name", is("Kurt"));
    }
    @Test
    @DisplayName("Get all persons 2")
    void testAllBody2(){
        Map<String, Map<String, Object>> jsonResponse = given()
                .when()
                .get("/person")
                .then()
                .extract()
                .jsonPath()
                .getMap("$");

        assertEquals("Kurt", jsonResponse.get("1").get("name"));
        assertEquals(23, jsonResponse.get("1").get("age"));
        assertEquals("Hanne", jsonResponse.get("2").get("name"));
        assertEquals(21, jsonResponse.get("2").get("age"));
        assertEquals("Tina", jsonResponse.get("3").get("name"));
        assertEquals(25, jsonResponse.get("3").get("age"));
    }

    @Test
    @DisplayName("Json PATH and DTOs")
    void testAllBody4(){
        Response response = given()
                .when()
                .get("/person");
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
