package utils;

import com.github.javafaker.Faker;
import model.swager.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class RandomTestData {
    private static Random random = new Random();
    private static Faker faker = new Faker();

    private static GamesItem getRandomGame() {
        SimilarDlc similarDlc = SimilarDlc.builder()
                .dlcNameFromAnotherGame(faker.funnyName().name())
                .isFree(false)
                .build();

        DlcsItem dlcsItem = DlcsItem.builder()
                .description(faker.food().dish())
                .dlcName(faker.gameOfThrones().dragon())
                .isDlcFree(false)
                .price(faker.random().nextInt(1, 500))
                .rating(faker.random().nextInt(10))
                .similarDlc(similarDlc)
                .build();

        Requirements requirements = Requirements.builder()
                .ramGb(faker.random().nextInt(4, 32))
                .osName("Windows")
                .hardDrive(faker.random().nextInt(20, 100))
                .videoCard("NVideo")
                .build();

        return GamesItem.builder()
                .genre(faker.book().genre())
                .price(faker.random().nextInt(700))
                .description("World of Tank")
                .company(faker.company().name())
                .isFree(false)
                .title(faker.chuckNorris().fact())
                .rating(faker.random().nextInt(10))
                .publishDate(LocalDateTime.now().toString())
                .requiredAge(faker.random().nextBoolean())
                .tags(Arrays.asList("shoter", "quest"))
                .dlcs(Collections.singletonList(dlcsItem))  //  будет список из одного элемента
                .requirements(requirements)
                .build();
    }

    public static FullUser getRandomUserWithGame() {
        int randomNumber = Math.abs(random.nextInt());
        GamesItem gamesItem = getRandomGame();

        return FullUser.builder()
                .login(faker.name().username() + randomNumber)
                .pass(faker.internet().password())
                .games(Collections.singletonList(gamesItem))
                .build();
    }

    public static FullUser getRandomUser() {
        int randomNumber = Math.abs(random.nextInt());
        return FullUser.builder()
                .login("telepneves" + randomNumber)
                .pass("qwerty" + randomNumber)
                .build();
    }

    public static FullUser getAdminUser() {
        return FullUser.builder()
                .login("admin")
                .pass("admin")
                .build();
    }
}
