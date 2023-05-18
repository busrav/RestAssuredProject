import POJO.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class GoRestUsersTest {

    public String createRandomName(){
      return RandomStringUtils.randomAlphabetic(8);
    }
    public String createRandomEmail(){
        return RandomStringUtils.randomAlphabetic(8).toLowerCase() + "@gmail.com";
    }

    RequestSpecification requestSpec;
    ResponseSpecification responseSpec;
    @BeforeClass
    public void setup(){
        baseURI = "https://gorest.co.in/public/v2/users";

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer 1af001eea739c1e81f3706080376ae9a3843eba7a3ea17647810eadf1d47c919")
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .log(LogDetail.BODY)
                .build();
    }
    @Test(enabled = false)
    public void createAUser(){
        given()  //preparation  (headers, parameters...)
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + createRandomName() + "\", \"email\": \"" + createRandomEmail() + "\", \"gender\": \"male\",\"status\": \"active\"}")
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
                .spec(responseSpec)
                .statusCode(201);
    }

    @Test(enabled = false)
    public void createAUserWithMaps(){
        Map<String,String> user = new HashMap<>();
        user.put("name", createRandomName());
        user.put("email", createRandomEmail());
        user.put("gender", "female");
        user.put("status", "inactive");

        given()  //preparation  (headers, parameters...)
//                .header("Authorization", "Bearer 1af001eea739c1e81f3706080376ae9a3843eba7a3ea17647810eadf1d47c919")
                .spec(requestSpec)
//                .contentType(ContentType.JSON)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("")
//                .post("https://gorest.co.in/public/v2/users")

                .then()
                .spec(responseSpec)
//                .log().body()
                .statusCode(201);
//                .contentType(ContentType.JSON)
    }

    User user;
    Response response;
    @Test
    public void createAUserWithObjects(){
        user = new User();
        user.setName(createRandomName());
        user.setEmail(createRandomEmail());
        user.setGender("female");
        user.setStatus("inactive");

       response =  given()  //preparation  (headers, parameters...)
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
                .spec(responseSpec)
                .statusCode(201)
                .extract().response();
    }

    /** Write create user negative test **/


    @Test(dependsOnMethods = "createAUserWithObjects", priority = 1)
    public void createUserNegativeTest(){
        User user = new User();
        user.setName(createRandomName());
        user.setEmail(response.path("email"));
        user.setGender("female");
        user.setStatus("inactive");

        given()  //preparation  (headers, parameters...)
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
                .spec(responseSpec)
                .statusCode(422)
                .body("[0].message",equalTo("has already been taken"));
    }

    /**get the user you created in createAUserWithObjects test**/


    @Test(dependsOnMethods = "createAUserWithObjects", priority = 2)
    public void getUserById() {


        given()

                .spec(requestSpec)
                .pathParam("userId", response.path("id"))

                .when()
                .get("/{userId}")

                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("email",equalTo(response.path("email")) )
                .body("id",equalTo(response.path("id")) )
                .body("name",equalTo(response.path("name")) );
    }

    /** Update the user you created in createAUserWithObjects **/

    @Test(dependsOnMethods = "createAUserWithObjects", priority = 3)
    public void updateUser(){
        user.setName("Jack Sparrow");

        given()

                .spec(requestSpec)
                .body(user)
                .pathParam("userId", response.path("id"))

                .when()
                .put("/{userId}")

                .then()
                .spec(responseSpec)
                .statusCode(200);
    }

    /** Delete the user we created in createAUserWithObjects **/
    @Test(dependsOnMethods = "createAUserWithObjects", priority = 4)
    public void deleteUser(){

        given()

                .spec(requestSpec)
                .pathParam("userId", response.path("id"))

                .when()
                .delete("/{userId}")

                .then()
                .statusCode(204);
    }

    /** create delete user negative test **/
    @Test(dependsOnMethods = {"createAUserWithObjects", "deleteUser"}, priority = 5)
    public void deleteUserNegativeTest(){

        given()

                .spec(requestSpec)
                .pathParam("userId", response.path("id"))

                .when()
                .delete("/{userId}")

                .then()
                .statusCode(404);
    }

    @Test
    public void getUsers(){
        Response response = given()
                .spec(requestSpec)
                .when()
                .get()


                .then()
                .spec(responseSpec)
                .statusCode(200)
                .extract().response();

        int userId0 = response.jsonPath().getInt("[0].id");
        int userId3 = response.jsonPath().getInt("[2].id");
        List<User> usersList = response.jsonPath().getList("",User.class);

        System.out.println("userId0 = " + userId0);
        System.out.println("userId3 = " + userId3);
        System.out.println("userList = " + usersList);
    }

}
