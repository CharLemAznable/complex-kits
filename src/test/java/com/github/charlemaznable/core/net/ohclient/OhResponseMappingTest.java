package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhDefaultErrorMappingDisabled;
import com.github.charlemaznable.core.net.ohclient.exception.OhClientError;
import com.github.charlemaznable.core.net.ohclient.exception.OhServerError;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusMapping;
import com.github.charlemaznable.core.net.ohclient.param.OhStatusSeriesMapping;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhResponseMappingTest {

    @SneakyThrows
    @Test
    public void testOhResponseMapping() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sampleNotFound":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    case "/sampleClientError":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.FORBIDDEN.value())
                                .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                    case "/sampleMappingNotFound":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    case "/sampleMappingClientError":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.FORBIDDEN.value())
                                .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                    case "/sampleServerError":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                }
                return new MockResponse().setBody("OK");
            }
        });
        mockWebServer.start(41180);

        val httpClient = getClient(MappingHttpClient.class);
        assertThrows(NotFoundException.class, httpClient::sampleNotFound);
        assertThrows(ClientErrorException.class, httpClient::sampleClientError);
        assertThrows(NotFoundException2.class, httpClient::sampleMappingNotFound);
        assertThrows(ClientErrorException2.class, httpClient::sampleMappingClientError);
        assertThrows(OhServerError.class, httpClient::sampleServerError);

        val defaultHttpClient = getClient(DefaultMappingHttpClient.class);
        assertThrows(OhClientError.class, defaultHttpClient::sampleNotFound);
        assertThrows(OhClientError.class, defaultHttpClient::sampleClientError);
        assertThrows(OhClientError.class, defaultHttpClient::sampleMappingNotFound);
        assertThrows(OhClientError.class, defaultHttpClient::sampleMappingClientError);
        assertThrows(OhServerError.class, defaultHttpClient::sampleServerError);

        val disabledHttpClient = getClient(DisabledMappingHttpClient.class);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleNotFound());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleClientError());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleMappingNotFound());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleMappingClientError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClient.sampleServerError());

        mockWebServer.shutdown();
    }

    @OhStatusMapping(status = HttpStatus.NOT_FOUND,
            exception = NotFoundException.class)
    @OhStatusSeriesMapping(statusSeries = HttpStatus.Series.CLIENT_ERROR,
            exception = ClientErrorException.class)
    @OhClient("${root}:41180")
    public interface MappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        @OhStatusMapping(status = HttpStatus.NOT_FOUND,
                exception = NotFoundException2.class)
        @OhStatusSeriesMapping(statusSeries = HttpStatus.Series.CLIENT_ERROR,
                exception = ClientErrorException2.class)
        String sampleMappingNotFound();

        @OhStatusMapping(status = HttpStatus.NOT_FOUND,
                exception = NotFoundException2.class)
        @OhStatusSeriesMapping(statusSeries = HttpStatus.Series.CLIENT_ERROR,
                exception = ClientErrorException2.class)
        String sampleMappingClientError();

        String sampleServerError();
    }

    @OhClient("${root}:41180")
    public interface DefaultMappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        String sampleMappingNotFound();

        String sampleMappingClientError();

        String sampleServerError();
    }

    @OhDefaultErrorMappingDisabled
    @OhClient("${root}:41180")
    public interface DisabledMappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        String sampleMappingNotFound();

        String sampleMappingClientError();

        String sampleServerError();
    }

    public static class NotFoundException extends RuntimeException {

        private static final long serialVersionUID = -6500698707558354057L;
    }

    public static class ClientErrorException extends RuntimeException {

        private static final long serialVersionUID = -3870950937253448454L;
    }

    public static class NotFoundException2 extends RuntimeException {

        private static final long serialVersionUID = 8138254149072329848L;
    }

    public static class ClientErrorException2 extends RuntimeException {

        private static final long serialVersionUID = -7855725166604686605L;
    }
}
