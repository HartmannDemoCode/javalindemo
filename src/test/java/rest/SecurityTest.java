package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.SecurityRoutes;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SecurityTest {

    private static ApplicationConfig appConfig;
    private static EntityManagerFactory emfTest;
    private static ObjectMapper jsonMapper = new ObjectMapper();


    @BeforeAll
    static void setUpAll() {
        RestAssured.baseURI = "http://localhost:7777";

        HibernateConfig.setTestMode(true); // IMPORTANT leave this at the very top of this method in order to use the test database
        RestRoutes restRoutes = new RestRoutes();

        // Setup test database using docker testcontainers
        emfTest = HibernateConfig.getEntityManagerFactory();

        // Start server
        appConfig = ApplicationConfig.
                getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setErrorHandling()
                .setGeneralExceptionHandling()
                .setRoutes(restRoutes.getOpenRoutes())
                .setRoutes(SecurityRoutes.getSecurityRoutes())
                .setRoutes(SecurityRoutes.getSecuredRoutes())
                .setRoutes(restRoutes.personEntityRoutes) // A different way to get the EndpointGroup. Getting data from DB
                .setCORS()
                .setApiExceptionHandling()
                .startServer(7777)
        ;
    }

    @AfterAll
    static void afterAll() {
        appConfig.stopServer();
        HibernateConfig.setTestMode(false);
    }

    @BeforeEach
    void setUpEach() {
        // Setup test database for each test
        new TestUtils().createUsersAndRoles(emfTest);
        // Setup DB Persons and Addresses
        new TestUtils().createPersonEntities(emfTest);
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/open/person").then().statusCode(200);
    }
    private static String securityToken;

    private static void login(String username, String password) {
        ObjectNode objectNode = jsonMapper.createObjectNode()
                .put("username", username)
                .put("password", password);
        String loginInput = objectNode.toString();
        securityToken = given()
                .contentType("application/json")
                .body(loginInput)
                //.when().post("/api/login")
                .when().post("/auth/login")
                .then()
                .extract().path("token");
        System.out.println("TOKEN ---> " + securityToken);
    }
    @Test
    @DisplayName("Test login for user")
    public void testRestForUser() {
        login("user", "user123");
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Bearer "+securityToken)
                .when()
                .get("/protected/user_demo").then()
                .statusCode(200)
                .body("msg", equalTo("Hello from USER Protected"));
    }

    @Test
    @DisplayName("Test login for admin not authorized")
    public void testRestForUserProtection() {
        login("user", "user123");
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Bearer "+securityToken)
                .when()
                .get("/protected/admin_demo").then()
                .statusCode(403)
                .body("msg", equalTo("Unauthorized with roles: [ADMIN]"));
    }

    @Test
    @DisplayName("Test CORS Headers")
    public void testCorsHeaders() {
        given()
                .when()
                .get("/open/person")
                .then()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .statusCode(200);
    }

    @Test
    @DisplayName("Test CORS Preflight against a protected route")
    public void testCorsPreflight() {
        given()
                .when()
                .header("Access-Control-Request-Method", "POST")
//                .header("Origin", "http://localhost:7777")
                .options("/protected/admin_demo")
                .then()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .statusCode(200);
    }

    @Test
    @DisplayName("Test Entities from DB")
    public void testEntitiesFromDB() {
        login("admin", "admin123");
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Bearer "+securityToken)
                .when()
                .get("/person").then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }
    @Test
    @DisplayName("Test POST to Person Entities not allowed for User role")
    public void testEntitiesFromDBNotAllowed() {
        login("user", "user123");
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Bearer "+securityToken)
                .when()
                .post("/person").then()
                .statusCode(403); // Forbidden
    }
}
