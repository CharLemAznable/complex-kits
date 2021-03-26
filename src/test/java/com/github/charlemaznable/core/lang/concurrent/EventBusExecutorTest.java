package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EventBusExecutorTest {

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

        val testEventBusCachedSubscriber = new TestEventBusCachedSubscriber();
        val testEventBusCachedExecutor2 = new EventBusCachedExecutor(testEventBusCachedSubscriber) {};
        testEventBusCachedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedSubscriber.message)));

        testEventBusCachedExecutor2.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusCachedSubscriber.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusCachedSubscriber.message)));
    }

    @Test
    public void testEventBusFixedExecutor() {
        val testEventBusFixedExecutor = new TestEventBusFixedExecutor();
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

        val testEventBusFixedSubscriber = new TestEventBusFixedSubscriber();
        val testEventBusFixedExecutor2 = new EventBusFixedExecutor(testEventBusFixedSubscriber) {};
        testEventBusFixedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedSubscriber.message)));

        testEventBusFixedExecutor2.post("delay", 1, TimeUnit.SECONDS);
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "test".equals(testEventBusFixedSubscriber.message)));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(2000)).until(() ->
                        "delay".equals(testEventBusFixedSubscriber.message)));
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

    static class TestEventBusCachedSubscriber {

        private String message;

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

    static class TestEventBusFixedSubscriber {

        private String message;

        @Subscribe
        public void testMethod(String message) {
            this.message = message;
        }
    }
}
