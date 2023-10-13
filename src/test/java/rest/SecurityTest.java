package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.cphbusiness.data.HibernateConfig;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.Role;
import dk.cphbusiness.security.SecurityRoutes;
import dk.cphbusiness.security.User;
import io.javalin.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SecurityTest {

    ObjectMapper objectMapper = new ObjectMapper();
    private static ApplicationConfig appConfig;
    private static EntityManagerFactory emfTest;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static Object adminToken;
    private static Object userToken;
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
                .checkSecurityRoles()
                .setRoutes(SecurityRoutes.getSecuredRoutes())
                .setApiExceptionHandling()
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
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get(BASE_URL+"/person").then().statusCode(200);
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
                .when().post(BASE_URL+"/auth/login")
                .then()
                .extract().path("token");
        System.out.println("TOKEN ---> " + securityToken);
    }
    @Test
    public void testRestForAdmin() {
        login("user", "user123");
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Baerer "+securityToken)
                .when()
                .get(BASE_URL+"/protected/user_demo").then()
                .statusCode(200)
                .body("msg", equalTo("Hello from USER Protected"));
    }

}
