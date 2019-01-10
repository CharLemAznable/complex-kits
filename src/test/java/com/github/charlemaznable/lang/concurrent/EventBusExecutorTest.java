package com.github.charlemaznable.lang.concurrent;

import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventBusExecutorTest {

    @SneakyThrows
    @Test
    public void testEventBusExecutor() {
        TestEventBusCachedExecutor testEventBusCachedExecutor = new TestEventBusCachedExecutor();
        testEventBusCachedExecutor.post("test");
        Thread.sleep(10);
        assertEquals("test", testEventBusCachedExecutor.message);

        testEventBusCachedExecutor.post("delay", 1, TimeUnit.SECONDS);
        Thread.sleep(10);
        assertEquals("test", testEventBusCachedExecutor.message);
        Thread.sleep(1000);
        assertEquals("delay", testEventBusCachedExecutor.message);
    }

    public static class TestEventBusCachedExecutor extends EventBusCachedExecutor {

        private String message;

        @Subscribe
        public void testMethod(String message) {
            this.message = message;
        }
    }
}
