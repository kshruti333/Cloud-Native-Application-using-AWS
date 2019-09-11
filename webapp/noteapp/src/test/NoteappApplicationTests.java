package src.test;

import csye6225.cloud.noteapp.NoteApp;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NoteApp.class)

public class NoteappApplicationTests {

    @Test
    public void testForAuth() {
        RestAssured.baseURI = "http://localhost:8080";
        given().auth().preemptive().basic("kosaraju_uuu@rediffmail.com", "Kosaraju@1234")
                .when()
                .get("/")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testForUnAuth() {
        RestAssured.baseURI = "http://localhost:8080";
        given().auth().preemptive().basic("kosaraju_uuu@rediffmail.com", "Kosaraju@123")
                .when()
                .get("/")
                .then()
                .assertThat()
                .statusCode(401);
    }

    /* A Test case to verify successful user account creation
    * Takes new user email id and password as the inputs
    * The credentials are passed as Map object
    * Throws error if the user already exists */
    @Test
    public void testCreateUser() {
        RestAssured.baseURI = "http://localhost:8080";
        RestAssured.port = 8080;
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("email", "lastkingasabhilash@yahoo.com");
        userDetails.put("password", "abhi@1234");
        Response response = given().contentType("application/json").accept("application/json").
                body(userDetails).when().post("/user/register").then().statusCode(200).
                contentType("application/json").extract().response();

    }



}