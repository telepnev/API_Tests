package api.swagerTests;

import api.listener.CustomTpl;
import io.qameta.allure.Attachment;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.FileServes;

import java.io.File;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;

public class FileTests {
    private static FileServes fileServes;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());
        fileServes = new FileServes();
    }

    @Attachment(value = "downloaded", type = "image/png")
    private byte[] attachFile(byte[] bytes) {
        return bytes;
    }

    @Test
    public void positiveDownloadTest() {
        byte[] file = fileServes.downloadBaseImage().asResponse().asByteArray();
        attachFile(file);
        File expectFile = new File("src/test/resources/threadqa.jpeg");
        Assertions.assertEquals(expectFile.length(), file.length);
    }

    @Test
    public void positiveUploadFile() {
        File expectFile = new File("src/test/resources/threadqa.jpeg");

        fileServes.uploadFile(expectFile)
                .should(hasStatusCode(200))
                .should(hasMessage("file uploaded to server"));

        byte[] actualFile = fileServes.downloadLastFile().asResponse().asByteArray();
        Assertions.assertTrue(actualFile.length != 0);
        Assertions.assertEquals(expectFile.length(), actualFile.length);
        attachFile(actualFile);

    }
}
