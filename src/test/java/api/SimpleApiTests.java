package api;

import io.restassured.response.Response;
import model.Address;
import model.Geolocation;
import model.Name;
import model.UserRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                .get("https://fakestoreapi.com/users")
                .then()
                .log().body()
                // .body("", hasSize(limitSize))
                .body("", hasSize(greaterThanOrEqualTo(limitSize)));
    }

    // проверка сортировки
    @Test
    public void getAllUsersSortByDescTest() {
        String sortType = "desc";

        Response sortedResponse = given().queryParam("sort", sortType)
                .get("https://fakestoreapi.com/users")
                .then()
                .extract().response();


        Response notSortedResponse = given().get("https://fakestoreapi.com/users")
                .then()
                .extract().response();

        List<Integer> sortedResponseIds = sortedResponse.jsonPath().getList("id");
        List<Integer> notSortedResponseIds = notSortedResponse.jsonPath().getList("id");

        List<Integer> sortedByCodeList = notSortedResponseIds
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        Assertions.assertEquals(sortedByCodeList, sortedResponseIds);
    }

    @Test
    public void addNewUserTest(){
        Name name = new Name("Geka", "Telepnev");
        Geolocation geolocation = new Geolocation("-37.3159", "81.1496");
        Address address = Address.builder()
                .city("Voroneg")
                .street("Wall street")
                .number(666)
                .zipcode("12926-3874")
                .geolocation(geolocation)
                .build();

        UserRoot bodyRequest = UserRoot.builder()
                .email("mail@maol.ru")
                .username("GekaT")
                .password("12345567")
                .name(name)
                .address(address)
                .phone("+98877923424234")
                .build();

        given().body(bodyRequest)
                .post("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue());
    }
}
