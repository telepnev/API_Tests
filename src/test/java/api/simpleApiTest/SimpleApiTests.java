package api.simpleApiTest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.Address;
import model.Geolocation;
import model.Name;
import model.UserRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

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
    public void addNewUserTest() {
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


    private UserRoot getUser() {
        Name name = new Name("Geka", "Telepnev");
        Geolocation geolocation = new Geolocation("-37.3159", "81.1496");
        Address address = Address.builder()
                .city("Voroneg")
                .street("Wall street")
                .number(666)
                .zipcode("12926-3874")
                .geolocation(geolocation)
                .build();

        return UserRoot.builder()
                .email("mail@maol.ru")
                .username("GekaT")
                .password("12345567")
                .name(name)
                .address(address)
                .phone("+98877923424234")
                .build();
    }

    @Test
    public void updateUserTest() {
// нужно получить юзера, изменить у него параметр,
// в put  подставить его ID и затем сверить что старый пароль не равен новому
        UserRoot user = getUser();
        String oldPassword = user.getPassword();
        user.setPassword("newPassword111");

        given().body(user)
                .put("https://fakestoreapi.com/users/7")
                .then().log().all();
    }

    @Test
    public void deleteUserTest() {
        given().delete("https://fakestoreapi.com/users/6")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void loginUserTest() {
        Map<String, String> auth = new HashMap<>();
        auth.put("username", "johnd");
        auth.put("password", "m38rmF$");

        given().contentType(ContentType.JSON)
                .body(auth)
                .post("https://fakestoreapi.com/auth/login")
                .then().log().all()
                .statusCode(2002)
                .body("token", notNullValue());

    }
    @Test
    public void loginUser88Test() {
        Map<String, String> auth = new HashMap<>();
        auth.put("username", "johnd");
        auth.put("password", "m38rmF$");

        given().contentType(ContentType.JSON)
                .body(auth)
                .post("https://fakestoreapi.com/auth/login")
                .then().log().all()
                .statusCode(2002)
                .body("token", notNullValue());

    }
}
