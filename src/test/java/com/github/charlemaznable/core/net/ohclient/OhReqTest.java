package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.CommonReqTest;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.ClientErrorException;
import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.NotFoundException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OhReqTest extends CommonReqTest {

    @SneakyThrows
    @Test
    public void testOhReq() {
        startMockWebServer(41103);

        val ohReq1 = new OhReq("http://127.0.0.1:41103/sample1")
                .acceptCharset(ISO_8859_1)
                .contentFormat(new FormContentFormatter())
                .header("AAA", "aaa")
                .headers(of("AAA", null, "BBB", "bbb"))
                .parameter("CCC", "ccc");
        assertEquals("Sample1", ohReq1.get());

        val ohReq2 = new OhReq()
                .req("http://127.0.0.1:41103/sample2")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"));
        assertEquals("Sample2", ohReq2.post());

        val ohReq3 = new OhReq("http://127.0.0.1:41103")
                .req("/sample3?DDD=ddd")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future3 = ohReq3.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future3::isDone);
        assertEquals("Sample3", future3.get());

        val ohReq4 = new OhReq("http://127.0.0.1:41103")
                .req("/sample4")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future4 = ohReq4.postFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future4::isDone);
        assertEquals("Sample4", future4.get());

        val ohReq5 = new OhReq("http://127.0.0.1:41103/sample5");
        try {
            ohReq5.get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            ohReq5.parameter("AAA", "aaa").get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        val ohReq6 = new OhReq("http://127.0.0.1:41103/sample6")
                .statusErrorMapping(HttpStatus.NOT_FOUND, NotFoundException.class)
                .statusSeriesErrorMapping(HttpStatus.Series.CLIENT_ERROR, ClientErrorException.class);
        try {
            ohReq6.get();
        } catch (NotFoundException e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            ohReq6.parameter("AAA", "aaa").get();
        } catch (ClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        shutdownMockWebServer();
    }
}
