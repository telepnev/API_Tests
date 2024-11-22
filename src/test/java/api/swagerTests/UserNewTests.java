package api.swagerTests;

import api.listener.AdminUser;
import api.listener.AdminUserResolver;
import api.listener.CustomTpl;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import model.swager.FullUser;
import model.swager.Info;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import service.UserService;

import java.util.List;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static utils.RandomTestData.*;

@ExtendWith(AdminUserResolver.class)
public class UserNewTests {
    private static UserService userService;
    private FullUser user;
    private FullUser adminUser;

    @BeforeEach
    public void initTestUser() {
        user = getRandomUser();
        adminUser = getAdminUser();
    }

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        userService = new UserService();
    }


    @Test
    public void positiveAdminAuthTest() {
        String token = userService.auth(adminUser)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    public void positiveAdminAuthWithAnnotationTest(@AdminUser FullUser admin) {
        String token = userService.auth(admin)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    public void positiveRegisterUser201Test() {
        Response response = userService
                .register(user).asResponse();

        Info infoMessage = response.jsonPath().getObject("info", Info.class);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(infoMessage.getMessage())
                .as("Сообщение о ошибки было не верное")
                .isEqualTo("FAKE MESSAGE");
        softAssertions.assertThat(response.statusCode()).as("Статус код не был 201")
                .isEqualTo(201);
        softAssertions.assertAll();
    }

    @Test
    public void positiveRegisterUserWithGameTest() {
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));

    }

    @Test
    public void negativeRegisterLoginExist400Test() {
        userService.register(user);
        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Login already exist"));
    }

    @Test
    public void negativeRegisterNonPassword400Test() {
        user.setPass(null);

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Missing login or password"));
    }

    @Test
    public void positiveNewUserAuthTest() {
        userService.register(user)
                .should(hasStatusCode(201));

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);
    }

    @Test
    public void negativeAuthTest() {
        String token = userService.auth(user)
                .should(hasStatusCode(401))
                .asJwt();

        Assertions.assertNull(token);
    }

    @Test
    public void positiveGetUserInfoTest() {
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
        String token = userService.auth(adminUser).asJwt();

        String updatePassword = "newPassword123";
        userService.updatePass(updatePassword, token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant update base users"));
    }

    @Test
    public void negativeDeleteAdminTest() {
        String token = userService.auth(adminUser).asJwt();

        userService.deleteUser(token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant delete base users"));
    }

    @Test
    public void positiveDeleteUserTest() {
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
