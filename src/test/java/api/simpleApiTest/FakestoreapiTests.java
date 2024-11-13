package api.simpleApiTest;

import api.listener.CustomTpl;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import model.fakestoreapi.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;


import static io.restassured.RestAssured.given;


public class FakestoreapiTests {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
    }

    @Test
    public void getAllUsersTest() {
        given().get("/users")
                .then()
                .statusCode(200);
    }

    @Test
    public void getSingleUserTest() {
        int userId = 2;
        String name = "david";

        UserRoot response = given()
                .pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .log().body()
                .statusCode(200)
                .extract()
                .as(UserRoot.class);

        Assertions.assertEquals(userId, response.getId());
        Assertions.assertEquals(name, response.getName().firstname);
        Assertions.assertNotNull(response.getEmail());
        Assertions.assertTrue(response.getAddress().getGeolocation()
                .getMyLong().matches("\\d{2}.\\d{4}"));
        Assertions.assertTrue(response.getAddress().getZipcode().
                matches("\\d{5}-\\d{4}"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    public void getAllUsersWithLimitTest(int limitSize) {

        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                // .extract().jsonPath().getList("", UserRoot.class);
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        Assertions.assertEquals(limitSize, users.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 20})
    public void getAllUsersWithErrorsParamsTest(int limitSize) {

        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                // .extract().jsonPath().getList("", UserRoot.class);
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        Assertions.assertNotEquals(limitSize, users.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100})
    public void getAllUsersWithLimitErrorsTest(int limitSize) {

        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                // .extract().jsonPath().getList("", UserRoot.class);
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        Assertions.assertEquals(limitSize, users.size());
    }

    // проверка сортировки
    @Test
    public void getAllUsersSortByDescTest() {
        String sortType = "desc";

        List<UserRoot> usersSorted = given()
                .queryParam("sort", sortType)
                .get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {
                });


        List<UserRoot> usersNotSorted = given()
                .get("/users")
                .then()
                .extract().as(new TypeRef<List<UserRoot>>() {
                });

        List<Integer> sortedResponseIds = usersSorted
                .stream().map(UserRoot::getId)
                .collect(Collectors.toList());

        List<Integer> sortedByCodeList = usersNotSorted
                .stream().map(UserRoot::getId).sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        Assertions.assertNotEquals(usersSorted, usersNotSorted);
        Assertions.assertEquals(sortedResponseIds, sortedByCodeList);
    }

    @Test
    public void addNewUserTest() {
        UserRoot user = getUser();

        Integer userId = given().body(user)
                .post("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        Assertions.assertNotNull(userId);
    }

    @Test
    public void updateUserTest() {

        UserRoot user = getUser();
        String oldPassword = user.getPassword();
        user.setPassword("newPassword111");

        UserRoot updateUser = given()
                .body(user)
                // так как в классе UserRoot не устанавливается ID
//                .pathParam("userId", user.getId())
//                .put("/users/{userId}")
//                .then()
//                .extract().as(UserRoot.class);
                .put("/users/7")
                .then()
                .extract().as(UserRoot.class);

        Assertions.assertNotEquals(updateUser.getPassword(), oldPassword);
    }

    @Test
    public void loginUserTest() {
        AuthData authData = new AuthData("johnd", "m38rmF$");

       String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

       Assertions.assertNotNull(token);
    }

    @Test
    public void loginUserErrorsTest() {
        AuthData authData = new AuthData("johnd77", "m38rmF$77");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    private UserRoot getUser() {
        Random random = new Random();

        Name name = new Name("Geka", "Telepnev");
        Geolocation geolocation = new Geolocation("-37.3159", "81.1496");
        Address address = Address.builder()
                .city("Voroneg1")
                .street("Wall street")
                .number(random.nextInt(100))
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

}
