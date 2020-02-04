package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.ClientErrorException;
import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.NotFoundException;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.FormContentFormat;
import com.github.charlemaznable.core.net.ohclient.exception.OhError;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;

import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.lang.Str.isNull;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

public class OhReqTest {

    @SneakyThrows
    @Test
    public void testOhReq() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                val requestUrl = request.getRequestUrl();
                switch (requestUrl.encodedPath()) {
                    case "/sample1":
                        val acceptCharset = request.getHeader(ACCEPT_CHARSET);
                        assertEquals(ISO_8859_1.name(), acceptCharset);
                        val contentType = request.getHeader(CONTENT_TYPE);
                        assertTrue(contentType.startsWith(APPLICATION_FORM_URLENCODED_VALUE));
                        assertNull(request.getHeader("AAA"));
                        assertEquals("bbb", request.getHeader("BBB"));
                        assertEquals("ccc", requestUrl.queryParameter("CCC"));
                        assertEquals("GET", request.getMethod());
                        return new MockResponse().setBody("Sample1");

                    case "/sample2":
                        assertEquals("BBB=bbb", request.getBody().readUtf8());
                        assertEquals("POST", request.getMethod());
                        return new MockResponse().setBody("Sample2");

                    case "/sample3":
                        assertEquals("ddd", requestUrl.queryParameter("DDD"));
                        assertNull(requestUrl.queryParameter("AAA"));
                        assertEquals("bbb", requestUrl.queryParameter("BBB"));
                        assertNull(requestUrl.queryParameter("CCC"));
                        assertEquals("GET", request.getMethod());
                        return new MockResponse().setBody("Sample3");

                    case "/sample4":
                        assertEquals("CCC=ccc", request.getBody().readUtf8());
                        assertEquals("POST", request.getMethod());
                        return new MockResponse().setBody("Sample4");

                    case "/sample5":
                    case "/sample6":
                        if (isNull(requestUrl.queryParameter("AAA"))) {
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                        } else {
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.FORBIDDEN.value())
                                    .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                        }
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41103);

        val ohReq1 = new OhReq("http://127.0.0.1:41103/sample1")
                .acceptCharset(ISO_8859_1).contentFormat(new FormContentFormat())
                .header("AAA", "aaa").headers(of("AAA", null, "BBB", "bbb"))
                .parameter("CCC", "ccc");
        assertEquals("Sample1", ohReq1.get());

        val ohReq2 = new OhReq().req("http://127.0.0.1:41103/sample2")
                .parameter("AAA", "aaa").parameters(of("AAA", null, "BBB", "bbb"));
        assertEquals("Sample2", ohReq2.post());

        val ohReq3 = new OhReq("http://127.0.0.1:41103").req("sample3?DDD=ddd")
                .parameter("AAA", "aaa").parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future3 = ohReq3.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future3::isDone);
        assertEquals("Sample3", future3.get());

        val ohReq4 = new OhReq("http://127.0.0.1:41103").req("sample4")
                .parameter("AAA", "aaa").parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc");
        val future4 = ohReq4.postFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future4::isDone);
        assertEquals("Sample4", future4.get());

        val ohReq5 = new OhReq("http://127.0.0.1:41103/sample5");
        try {
            ohReq5.get();
        } catch (OhError e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            ohReq5.parameter("AAA", "aaa").get();
        } catch (OhError e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        val ohReq6 = new OhReq("http://127.0.0.1:41103/sample6")
                .statusMapping(HttpStatus.NOT_FOUND, NotFoundException.class)
                .statusSeriesMapping(HttpStatus.Series.CLIENT_ERROR, ClientErrorException.class);
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

        mockWebServer.shutdown();
    }
}
