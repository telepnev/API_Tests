package api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleApiTests {
    @Test
    public void getAllUsersTest() {
        given().get("https://fakestoreapi.com/users")
                .then()
                .log().body()
                .statusCode(200);
    }

    @Test
    public void getSingleUserTest() {
        int userId = 2;
        String name = "daVid";
        given().pathParam("userId", userId)
                .get("https://fakestoreapi.com/users/{userId}")
                .then()
                .log().body()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name.firstname", equalToIgnoringCase(name))
                .body("email", notNullValue(null))
                .body("address.geolocation.long", matchesPattern("\\d{2}.\\d{4}"))
                .body("address.zipcode", matchesPattern("\\d{5}-\\d{4}"));
    }

    @Test
    public void getAllUsersWithLimitTest() {
        int limitSize = 3;

        given().queryParam("limit", limitSize)
                .get("https://fakestoreapi.com/users/")
                .then()
                .log().body()
                .body("", hasSize(limitSize));
    }
}
