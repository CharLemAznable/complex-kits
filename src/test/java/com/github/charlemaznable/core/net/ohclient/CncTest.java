package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.CncRequest;
import com.github.charlemaznable.core.net.common.CncResponse;
import com.github.charlemaznable.core.net.common.CncResponse.CncResponseImpl;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.ohclient.internal.OhDummy;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CncTest {

    @BeforeAll
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("Env", "ohclient",
                "port=41200");
    }

    @AfterAll
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @SneakyThrows
    @Test
    public void testCncClient() {
        StringSubstitutor ohMinerSubstitutor =
                onClass(OhDummy.class).field("ohMinerSubstitutor").get();
        ohMinerSubstitutor.setVariableResolver(
                minerAsSubstitutor("Env", "ohclient").getStringLookup());
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                val testResponse = new TestResponse();
                testResponse.setContent("content");
                return new MockResponse().setBody(json(testResponse));
            }
        });
        mockWebServer.start(41200);

        val client = getClient(CncClient.class);

        val response = client.sample1(new TestRequest());
        assertEquals("content", response.getContent());
        val nullResponse = client.sample1(null);
        assertTrue(nullResponse instanceof CncResponseImpl);

        val futureResponse = client.sample2(new TestRequest());
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureResponse::isDone);
        assertEquals("content", futureResponse.get().getContent());

        val pair = client.sample3(new TestRequest());
        assertEquals(HttpStatus.OK, pair.getLeft());
        assertEquals("content", pair.getRight().getContent());

        val futurePair = client.sample4(new TestRequest());
        await().forever().pollDelay(Duration.ofMillis(100)).until(futurePair::isDone);
        assertEquals(HttpStatus.OK, futurePair.get().getLeft());
        assertEquals("content", futurePair.get().getRight().getContent());

        val errorClient = getClient(CncErrorClient.class);

        assertThrows(OhException.class, errorClient::sample1);
        assertThrows(OhException.class, () -> errorClient.sample2(null));
        assertThrows(OhException.class, errorClient::sample3);

        mockWebServer.shutdown();
    }

    @OhClient
    @Mapping("${root}:${port}")
    public interface CncClient {

        <T extends CncResponse> T sample1(CncRequest<T> request);

        <T extends CncResponse> Future<T> sample2(CncRequest<T> request);

        <T extends CncResponse> Pair<HttpStatus, T> sample3(CncRequest<T> request);

        <T extends CncResponse> Future<Pair<HttpStatus, T>> sample4(CncRequest<T> request);
    }

    public static class TestRequest implements CncRequest<TestResponse> {

        @Override
        public Class<TestResponse> getResponseClass() {
            return TestResponse.class;
        }
    }

    public static class TestResponse implements CncResponse {

        @Getter
        @Setter
        private String content;
    }

    @OhClient
    @Mapping("${root}:${port}")
    public interface CncErrorClient {

        <T> T sample1();

        <T extends OtherResponse> Future<T> sample2(OtherRequest<T> request);

        <T extends Map> Pair<HttpStatus, T> sample3();
    }

    public interface OtherRequest<T extends OtherResponse> {

        Class<T> getResponseClass();
    }

    public interface OtherResponse {}
}