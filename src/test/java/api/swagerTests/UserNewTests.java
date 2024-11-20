package api.swagerTests;

import api.listener.CustomTpl;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import model.swager.FullUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.UserService;

import java.util.List;
import java.util.Random;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;

public class UserNewTests {
    private static UserService userService;
    public static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        random = new Random();
        userService = new UserService();
    }

    private FullUser getRandomUser() {
        int randomNumber = Math.abs(random.nextInt());

        return FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();
    }

    private FullUser getAdminUser() {
        return FullUser.builder()
                .login("admin")
                .pass("admin")
                .build();
    }


    @Test
    public void positiveRegisterUser201Test() {
        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));

    }

    @Test
    public void negativeRegisterLoginExist400Test() {
        FullUser user = getRandomUser();
        userService.register(user);
        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Login already exist"));
    }

    @Test
    public void negativeRegisterNonPassword400Test() {
        FullUser user = getRandomUser();
        user.setPass(null);

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Missing login or password"));
    }

    @Test
    public void positiveAdminAuthTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    public void positiveNewUserAuthTest() {
        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201));

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    public void negativeAuthTest() {
        FullUser user = getRandomUser();
        String token = userService.auth(user)
                .should(hasStatusCode(401))
                .asJwt();

        Assertions.assertNull(token);
    }

    @Test
    public void positiveGetUserInfoTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user).asJwt();
        userService.getUserInfo(token)
                .should(hasStatusCode(200));
    }

    @Test
    public void positiveGetUserInfoInvalidJwtTest() {
        userService.getUserInfo("invalid token 2342j2kl3j42l4j2")
                .should(hasStatusCode(401));

    }

    @Test
    public void negativeGetUserInfoWithoutJwtTest() {
        userService.getUserInfo()
                .should(hasStatusCode(401));
    }

    @Test
    public void positiveChangePasswordNewUserTest() {
        FullUser user = getRandomUser();
        String oldPassword = user.getPass();

        userService.register(user);
        String token = userService.auth(user).asJwt();

        String updatePassword = "newPassword123";

        userService.updatePass(updatePassword, token)
                .should(hasStatusCode(200))
                .should(hasMessage("User password successfully changed"));

        user.setPass(updatePassword);

        token = userService.auth(user).should(hasStatusCode(200)).asJwt();

        FullUser updateUser = userService.getUserInfo(token).as(FullUser.class);
        Assertions.assertNotEquals(oldPassword, updateUser.getPass());

    }

    @Test
    public void negativeChangeAdminPasswordTEst() {
        FullUser user = getAdminUser();
        String token = userService.auth(user).asJwt();

        String updatePassword = "newPassword123";
        userService.updatePass(updatePassword, token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant update base users"));
    }

    @Test
    public void negativeDeleteAdminTest() {
        FullUser user = getAdminUser();
        String token = userService.auth(user).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant delete base users"));
    }

    @Test
    public void positiveDeleteUserTest() {
        FullUser user = getRandomUser();
        userService.register(user);
        String token = userService.auth(user).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(200))
                .should(hasMessage("User successfully deleted"));

    }

    @Test
    public void positiveGetAllUsersTest() {
        List<String> listUsers = userService.getAllUsers().asList(String.class);
        Assertions.assertTrue(listUsers.size() >= 3);
    }
}
