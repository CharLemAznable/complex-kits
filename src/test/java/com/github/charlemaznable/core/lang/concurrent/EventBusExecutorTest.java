package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventBusExecutorTest {

    @SneakyThrows
    @Test
    public void testEventBusCachedExecutor() {
        var testEventBusCachedExecutor = new TestEventBusCachedExecutor();
        testEventBusCachedExecutor.post("test");
        Thread.sleep(100);
        assertEquals("test", testEventBusCachedExecutor.message);

        testEventBusCachedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals("test", testEventBusCachedExecutor.message);
        Thread.sleep(2000);
        assertEquals("delay", testEventBusCachedExecutor.message);

        testEventBusCachedExecutor = new TestEventBusCachedExecutor(null);
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
        var testEventBusFixedExecutor = new TestEventBusFixedExecutor();
        testEventBusFixedExecutor.post("test");
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedExecutor.message);

        testEventBusFixedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedExecutor.message);
        Thread.sleep(2000);
        assertEquals("delay", testEventBusFixedExecutor.message);

        testEventBusFixedExecutor = new TestEventBusFixedExecutor(null);
        testEventBusFixedExecutor.post("test");
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedExecutor.message);

        testEventBusFixedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals("test", testEventBusFixedExecutor.message);
        Thread.sleep(2000);
        assertEquals("delay", testEventBusFixedExecutor.message);
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
