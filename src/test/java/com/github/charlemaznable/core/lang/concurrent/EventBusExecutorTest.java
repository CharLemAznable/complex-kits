package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EventBusExecutorTest {

    @SneakyThrows
    @Test
    public void testEventBusCachedExecutor() {
        val testEventBusCachedExecutor = new TestEventBusCachedExecutor();
        testEventBusCachedExecutor.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedExecutor.message)));

        testEventBusCachedExecutor.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedExecutor.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusCachedExecutor.message)));

        val testEventBusCachedExecutor2 = new TestEventBusCachedExecutor(null);
        testEventBusCachedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedExecutor2.message)));

        testEventBusCachedExecutor2.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedExecutor2.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusCachedExecutor2.message)));
    }

    @SneakyThrows
    @Test
    public void testEventBusFixedExecutor() {
        val testEventBusFixedExecutor = new TestEventBusFixedExecutor(null);
        testEventBusFixedExecutor.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedExecutor.message)));

        testEventBusFixedExecutor.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedExecutor.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusFixedExecutor.message)));

        val testEventBusFixedExecutor2 = new TestEventBusFixedExecutor(null);
        testEventBusFixedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedExecutor2.message)));

        testEventBusFixedExecutor2.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedExecutor2.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusFixedExecutor2.message)));
    }

    static class TestEventBusCachedExecutor extends EventBusCachedExecutor {

        private String message;

        public TestEventBusCachedExecutor() {
            super();
        }

        public TestEventBusCachedExecutor(Object subscriber) {
            super(subscriber);
        }

        @Subscribe
        public void testMethod(String message) {
            this.message = message;
        }
    }

    static class TestEventBusFixedExecutor extends EventBusFixedExecutor {

        private String message;

        public TestEventBusFixedExecutor() {
            super();
        }

        public TestEventBusFixedExecutor(Object subscriber) {
            super(subscriber);
        }

        @Subscribe
        public void testMethod(String message) {
            this.message = message;
        }
    }
}
