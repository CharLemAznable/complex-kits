package com.github.charlemaznable.lang.concurrent;

import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventBusExecutorTest {

    @SneakyThrows
    @Test
    public void testEventBusCachedExecutor() {
        val testEventBusCachedExecutor = new TestEventBusCachedExecutor();
        testEventBusCachedExecutor.post("test");
        Thread.sleep(100);
        assertEquals("test", testEventBusCachedExecutor.message);

        testEventBusCachedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals("test", testEventBusCachedExecutor.message);
        Thread.sleep(2000);
        assertEquals("delay", testEventBusCachedExecutor.message);
    }

    @SneakyThrows
    @Test
    public void testEventBusFixedExecutor() {
        val testEventBusFixedSubscriber = new TestEventBusFixedSubscriber();
        val testEventBusFixedExecutor = new TestEventBusFixedExecutor(testEventBusFixedSubscriber);
        testEventBusFixedExecutor.post("test");
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedSubscriber.message);

        testEventBusFixedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedSubscriber.message);
        Thread.sleep(2000);
        assertEquals("delay", testEventBusFixedSubscriber.message);
    }

    public static class TestEventBusCachedExecutor extends EventBusCachedExecutor {

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

    public static class TestEventBusFixedExecutor extends EventBusFixedExecutor {

        public TestEventBusFixedExecutor() {
            super();
        }

        public TestEventBusFixedExecutor(Object subscriber) {
            super(subscriber);
        }
    }

    public static class TestEventBusFixedSubscriber {

        private String message;

        @Subscribe
        public void testMethod(String message) {
            this.message = message;
        }
    }
}
