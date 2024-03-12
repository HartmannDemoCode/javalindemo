package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.cphbusiness.persistence.HibernateConfig;
import dk.cphbusiness.rest.ApplicationConfig;
import dk.cphbusiness.rest.RestRoutes;
import dk.cphbusiness.security.SecurityRoutes;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

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
    @Test
    public void testLogRequest() {
        System.out.println("Testing logging request details");
        given()
                .log().all()
                .when().get("/parent")
                .then().statusCode(200);
    }

    @Test
    public void testLogResponse() {
        System.out.println("Testing logging response details");
        given()
                .when().get("/parent")
                .then().log().body().statusCode(200);
    }

//    @Test
//    public void testGetById()  {
//        given()
//                .contentType(ContentType.JSON)
////                .pathParam("id", p1.getId()).when()
//                .get("/parent/{id}",p1.getId())
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.OK_200.getStatusCode())
//                .body("id", equalTo(p1.getId()))
//                .body("name", equalTo(p1.getName()))
//                .body("children", hasItems(hasEntry("name","Joseph"),hasEntry("name","Alberta")));
//    }
//
//    @Test
//    public void testError() {
//        given()
//                .contentType(ContentType.JSON)
////                .pathParam("id", p1.getId()).when()
//                .get("/parent/{id}",999999999)
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
//                .body("code", equalTo(404))
//                .body("message", equalTo("The Parent entity with ID: 999999999 Was not found"));
//    }
//
//    @Test
//    public void testPrintResponse(){
//        Response response = given().when().get("/parent/"+p1.getId());
//        ResponseBody body = response.getBody();
//        System.out.println(body.prettyPrint());
//
//        response
//                .then()
//                .assertThat()
//                .body("name",equalTo("Henrik"));
//    }
//
//    @Test
//    public void exampleJsonPathTest() {
//        Response res = given().get("/parent/"+p1.getId());
//        assertEquals(200, res.getStatusCode());
//        String json = res.asString();
//        JsonPath jsonPath = new JsonPath(json);
//        assertEquals("Henrik", jsonPath.get("name"));
//    }
//
//    @Test
//    public void getAllParents() throws Exception {
//        List<ParentDTO> parentDTOs;
//
//        parentDTOs = given()
//                .contentType("application/json")
//                .when()
//                .get("/parent")
//                .then()
//                .extract()
//                .body()
//                .jsonPath()
//                .getList("", ParentDTO.class);
//
//        ParentDTO p1DTO = new ParentDTO(p1);
//        ParentDTO p2DTO = new ParentDTO(p2);
//        assertThat(parentDTOs, containsInAnyOrder(p1DTO, p2DTO));
//
//    }
//
//
//    @Test
//    public void postTest() {
//        Parent p = new Parent("Helge",45);
//        p.addChild(new Child("Josephine",3));
//        ParentDTO pdto = new ParentDTO(p);
//        String requestBody = GSON.toJson(pdto);
//
//        given()
//                .header("Content-type", ContentType.JSON)
//                .and()
//                .body(requestBody)
//                .when()
//                .post("/parent")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("id", notNullValue())
//                .body("name", equalTo("Helge"))
//                .body("children", hasItems(hasEntry("name","Josephine")));
//    }
//
//    @Test
//    public void updateTest() {
//        p2.addChild(c2);
//        p2.setAge(23);
//        ParentDTO pdto = new ParentDTO(p2);
//        String requestBody = GSON.toJson(pdto);
//
//        given()
//                .header("Content-type", ContentType.JSON)
//                .body(requestBody)
//                .when()
//                .put("/parent/"+p2.getId())
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("id", equalTo(p2.getId()))
//                .body("name", equalTo("Betty"))
//                .body("age", equalTo(23))
//                .body("children", hasItems(hasEntry("name","Alberta")));
//    }
//
//    @Test
//    public void testDeleteParent() {
//        given()
//                .contentType(ContentType.JSON)
//                .pathParam("id", p2.getId())
//                .delete("/parent/{id}")
//                .then()
//                .statusCode(200)
//                .body("id",equalTo(p2.getId()));
//    }
//
//    // More test tools from: https://www.baeldung.com/java-junit-hamcrest-guide
//    @Test
//    public void testListSize() {
//        System.out.println("Check size of list");
//        List<String> hamcrestMatchers = Arrays.asList(
//                "collections", "beans", "text", "number");
//        assertThat(hamcrestMatchers, hasSize(4));
//    }
//    @Test
//    public void testPropAndValue() {
//        System.out.println("Check for property and value on an entity instance");
//        Parent person = new Parent("Benjamin", 33);
//        assertThat(person, hasProperty("name", equalTo("Benjamin")));
//    }
//    @Test
//    public void testCompareObjects() {
//        System.out.println("Check if 2 instances has same property values (EG. use compare properties rather than objects");
//        Parent person1 = new Parent("Betty", 45);
//        Parent person2 = new Parent("Betty", 45);
//        assertThat(person1, samePropertyValuesAs(person2));
//    }
//    @Test
//    public void testToString(){
//        System.out.println("Check if obj.toString() creates the right output");
//        Parent person=new Parent("Billy", 89);
//        String str=person.toString();
//        assertThat(person,hasToString(str));
//    }
//
//    @Test
//    public void testMapContains() {
//        List<Parent> parents = Arrays.asList(
//                new Parent("Henrik",67),
//                new Parent("Henriette",57)
//        );
//        assertThat(parents.toArray(), arrayContainingInAnyOrder(parents.get(0),parents.get(1)));
//    }
//    @Test
//    public void testNumeric() {
//        System.out.println("Test numeric values");
//        assertThat(1.2, closeTo(1, 0.5));
//        assertThat(5, greaterThanOrEqualTo(5));
//
//        List<Integer> list = Arrays.asList(1, 2, 3);
//        int baseCase = 0;
//        assertThat(list, everyItem(greaterThan(baseCase)));
//    }
//    @Test
//    public void testMoreReadable() {
//        System.out.println("Use the IS, NOT etc keywords for readability");
//        String str1 = "text";
//        String str2 = "texts";
//        String str3 = "texts";
//        String str4 = "These are several texts in one sentence";
//        assertThat(str1, not(str2));
//        assertThat(str2, is(str3));
//        assertThat(str4, containsString(str2));
//    }
//

}
