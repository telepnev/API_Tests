package api.swagerTests;

import api.listener.CustomTpl;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import model.fakestoreapi.AuthData;
import model.swager.FullUser;
import model.swager.Info;
import model.swager.JwtAuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class UserTests {
    public static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        random = new Random();
    }

    @Test
    public void positiveRegisterUser201Test() {
        int randomNumber = Math.abs(random.nextInt());

        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath()
                .getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());
    }

    @Test
    public void negativeRegisterLoginExist400Test() {
        int randomNumber = Math.abs(random.nextInt());

        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201)
                .extract().jsonPath()
                .getObject("info", Info.class);

        Assertions.assertEquals("User created", info.getMessage());

        Info errorInfo = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(400)
                .extract().jsonPath()
                .getObject("info", Info.class);

        Assertions.assertEquals("Login already exist", errorInfo.getMessage());
    }

    @Test
    public void negativeRegisterNonPassword400Test() {
        int randomNumber = Math.abs(random.nextInt());

        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Missing login or password", info.getMessage());
    }

    @Test
    public void positiveAdminAuthTest() {
        JwtAuthData jwtAuthData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    @Test
    public void positiveNewUserAuthTest() {
        int randomNumber = Math.abs(random.nextInt());

        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();

        given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201);

        JwtAuthData jwtAuthData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }

    @Test
    public void negativeAuthTest() {
        JwtAuthData jwtAuthData = new JwtAuthData("user.getLogin", "user.getPass");

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(401)
                .extract().jsonPath().getString("token");

        Assertions.assertNull(token);
    }

    @Test
    public void positiveGetUserInfoTest() {
        int randomNumber = Math.abs(random.nextInt());
        //Создали юзера
        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();

        //Зарегились
        given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then()
                .statusCode(201);

        //Получили токен
        JwtAuthData jwtAuthData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        given().auth().oauth2(token)
                .get("/api/user")
                .then()
                .statusCode(200);

    }

    @Test
    public void positiveGetUserInfoInvalidJwtTest() {
        //Получили токен
        JwtAuthData jwtAuthData = new JwtAuthData("user.getLogin", "user.getPass");

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(401)
                .extract().jsonPath().getString("token");

        given().auth().oauth2(token)
                .get("/api/user")
                .then()
                .statusCode(200);

    }

    @Test
    public void negativeGetUserInfoWithoutJwtTest() {
        given()
                .get("/api/user")
                .then()
                .statusCode(401);
    }

    @Test
    public void positiveChangePasswordNewUserTest() {
        //Создали юзера
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty123")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData jwtAuthData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        // Меняем пароль
        Map<String, String> newPassword = new HashMap<>();
        String updatePassword = "newPassword123";
        newPassword.put("password", updatePassword);

        Info updatePasswordInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(newPassword)
                .put("/api/user")
                .then()
                .statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User password successfully changed", updatePasswordInfo.getMessage());


        // Авторизуемся с новым паролем
        jwtAuthData.setPassword(updatePassword);  // логин остается прежний, меняю только пароль!
        token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        FullUser updateUserPassword = given().auth().oauth2(token)
                .get("/api/user")
                .then()
                .statusCode(200)
                .extract().as(FullUser.class);

        Assertions.assertNotEquals(user.getPass(), updateUserPassword.getPass());
    }

    @Test
    public void negativeChangeAdminPasswordTEst() {

        JwtAuthData jwtAuthData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        // Меняем пароль
        Map<String, String> newPassword = new HashMap<>();
        String updatePassword = "newPassword123";
        newPassword.put("password", updatePassword);

        Info updatePasswordInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(newPassword)
                .put("/api/user")
                .then()
                .statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("Cant update base users", updatePasswordInfo.getMessage());
    }

    @Test
    public void negativeChangeExistPasswordUserTest() {
        //Создали юзера
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty123")
                .build();

        Info info = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User created", info.getMessage());

        JwtAuthData jwtAuthData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        // Меняем пароль
        Map<String, String> newPassword = new HashMap<>();
        String updatePassword = "qwerty123";
        newPassword.put("password", updatePassword);

        Info updatePasswordInfo = given().contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(newPassword)
                .put("/api/user")
                .then()
                .statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User password successfully changed", updatePasswordInfo.getMessage());


        // Авторизуемся с новым паролем
        jwtAuthData.setPassword(updatePassword);  // логин остается прежний, меняю только пароль!
        token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        FullUser updateUserPassword = given().auth().oauth2(token)
                .get("/api/user")
                .then()
                .statusCode(200)
                .extract().as(FullUser.class);

        // ошибка, есть возможность сменить пароль на предыдущий
        Assertions.assertNotEquals(user.getPass(), updateUserPassword.getPass());
    }

    @Test
    public void negativeDeleteAdminTest() {
        JwtAuthData jwtAuthData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Info info = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("Cant delete base users", info.getMessage());
    }

    @Test
    public void positiveDeleteUserTest() {
        //Создали юзера
        int randomNumber = Math.abs(random.nextInt());
        FullUser user = FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty123")
                .build();

        Info infoSignup = given().contentType(ContentType.JSON)
                .body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);
        Assertions.assertEquals("User created", infoSignup.getMessage());

        //Получаем токен
        JwtAuthData jwtAuthData = new JwtAuthData(user.getLogin(), user.getPass());
        String token = given().contentType(ContentType.JSON)
                .body(jwtAuthData)
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Info infoDelete = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User successfully deleted", infoDelete.getMessage());
    }

    @Test
    public void positiveGetAllUsersTest() {
      List<String> listUsers = given().get("/api/users")
                .then().statusCode(200)
                .extract().as(new TypeRef<List<String>>() {}); // new TypeRef<List<String>>() {} - если нужен просто СПИСОК чего либо
        Assertions.assertTrue(listUsers.size() >= 3);
    }
}
