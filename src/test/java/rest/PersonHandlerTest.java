package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.dtos.SimplePersonDTO;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.SecurityRoutes;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class PersonHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static ApplicationConfig appConfig;
    private static final String BASE_URL = "http://localhost:7777";

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
                .setRoutes(restRoutes.getOpenRoutes())
                .checkSecurityRoles() // needed for putting the ROLE.ANYONE on the open routes (necessary when these are used with authentication)
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
        given().when().get("/open/person").peek().then().statusCode(200);
    }

//    @Test
//    @DisplayName("Get person 1")
//    void getOne() {
//
//        given()
////                .header("Authorization", adminToken)
//                .contentType("application/json")
//                .when()
//                .get("/open/1")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("name", equalTo("Kurt"))
//                .body("age", equalTo(23));
//    }
    @Test
    @DisplayName("Get All Persons")
    void getAll() {

        given()
                .contentType("application/json")
                .when()
                .get("/open/person")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(6));
//                .body("1.name", equalTo("Kurt"));
    }
//    @Test
//    @DisplayName("Get all persons check first person")
//    void testAllBody(){
//        // given, when, then
//        given()
//                .when()
//                .get("/open")
//                .prettyPeek()
//                .then()
//                .body("1.name", is("Kurt"));
//    }

//    @Test
//    @DisplayName("Json PATH and DTOs")
//    void testAllBody4(){
//        Response response = given()
//                .when()
//                .get("/open");
//        JsonPath jsonPath = response.jsonPath();
//
//        // Get the map of persons from the outer json object
//        Map<String, Map<String, Object>> personsMap = jsonPath.getMap("$");
//
//        // Convert the map of persons to an array of SimplePersonDTO
//        SimplePersonDTO[] persons = personsMap.values()
//                .stream()
//                .map(personMap -> objectMapper.convertValue(personMap, SimplePersonDTO.class))
//                .toArray(SimplePersonDTO[]::new);
//
//        assertEquals(5, persons.length);
//    }
}
