package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.ScheduledDispatcherDelegate;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static java.util.Objects.isNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventBusExecutorTest {

    @Test
    public void testEventBusCachedExecutor() {
        val testEventBusCachedExecutor = new TestEventBusCachedExecutor();
        testEventBusCachedExecutor.delegate(new ScheduledDispatcherDelegate() {
            @Override
            public long schedulePeriod() {
                return 10L;
            }
        });
        testEventBusCachedExecutor.suspend();
        assertTrue(testEventBusCachedExecutor.suspended());

        testEventBusCachedExecutor.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(1000)).until(() ->
                        isNull(testEventBusCachedExecutor.message)));

        testEventBusCachedExecutor.resume();
        assertFalse(testEventBusCachedExecutor.suspended());

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
        testEventBusCachedExecutor2.delegate(new ScheduledDispatcherDelegate() {
            @Override
            public long schedulePeriod() {
                return 10L;
            }
        });
        testEventBusCachedExecutor2.suspend();
        assertTrue(testEventBusCachedExecutor2.suspended());

        testEventBusCachedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(1000)).until(() ->
                        isNull(testEventBusCachedSubscriber.message)));

        testEventBusCachedExecutor2.resume();
        assertFalse(testEventBusCachedExecutor2.suspended());

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
        testEventBusFixedExecutor.delegate(new ScheduledDispatcherDelegate() {
            @Override
            public long schedulePeriod() {
                return 10L;
            }
        });
        testEventBusFixedExecutor.suspend();
        assertTrue(testEventBusFixedExecutor.suspended());

        testEventBusFixedExecutor.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(1000)).until(() ->
                        isNull(testEventBusFixedExecutor.message)));

        testEventBusFixedExecutor.resume();
        assertFalse(testEventBusFixedExecutor.suspended());

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
        testEventBusFixedExecutor2.delegate(new ScheduledDispatcherDelegate() {
            @Override
            public long schedulePeriod() {
                return 10L;
            }
        });
        testEventBusFixedExecutor2.suspend();
        assertTrue(testEventBusFixedExecutor2.suspended());

        testEventBusFixedExecutor2.post("test");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(1000)).until(() ->
                        isNull(testEventBusFixedSubscriber.message)));

        testEventBusFixedExecutor2.resume();
        assertFalse(testEventBusFixedExecutor2.suspended());

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

    @Test
    public void testEventBusSequenceDispatch() {
        val testSequenceDispatchEventBus = new TestSequenceDispatchEventBus();
        testSequenceDispatchEventBus.delegate(new ScheduledDispatcherDelegate() {
            @Override
            public void configExecutorBeforeDispatch(@Nonnull Executor executor) {
                val threadPoolExecutor = (ThreadPoolExecutor) executor;
                val poolSize = testSequenceDispatchEventBus.poolSize.get();
                if (poolSize >= threadPoolExecutor.getMaximumPoolSize()) {
                    threadPoolExecutor.setMaximumPoolSize(poolSize);
                    threadPoolExecutor.setCorePoolSize(poolSize);
                } else {
                    threadPoolExecutor.setCorePoolSize(poolSize);
                    threadPoolExecutor.setMaximumPoolSize(poolSize);
                }
            }

            @Override
            public long schedulePeriod() {
                return 10L;
            }
        });

        testSequenceDispatchEventBus.post("1");
        testSequenceDispatchEventBus.post("1");
        testSequenceDispatchEventBus.post("1");
        testSequenceDispatchEventBus.post("1");
        testSequenceDispatchEventBus.post("1");
        testSequenceDispatchEventBus.post("2");
        testSequenceDispatchEventBus.post("2");
        awaitForMillis(100);
        assertTrue(testSequenceDispatchEventBus.cancel("1"));
        assertTrue(testSequenceDispatchEventBus.cancelAll("2"));
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(400)).until(() ->
                        "1111".equals(testSequenceDispatchEventBus.message.toString())));
        assertEquals(1, testSequenceDispatchEventBus.max);

        testSequenceDispatchEventBus.max = 0;
        testSequenceDispatchEventBus.message.setLength(0);
        testSequenceDispatchEventBus.poolSize.incrementAndGet();

        testSequenceDispatchEventBus.post("2");
        testSequenceDispatchEventBus.post("2");
        testSequenceDispatchEventBus.post("2");
        testSequenceDispatchEventBus.post("2");
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(200)).until(() ->
                        "2222".equals(testSequenceDispatchEventBus.message.toString())));
        assertEquals(2, testSequenceDispatchEventBus.max);

        testSequenceDispatchEventBus.max = 0;
        testSequenceDispatchEventBus.message.setLength(0);
        testSequenceDispatchEventBus.poolSize.decrementAndGet();

        testSequenceDispatchEventBus.post("3");
        testSequenceDispatchEventBus.post("3");
        testSequenceDispatchEventBus.post("3");
        testSequenceDispatchEventBus.post("3");
        awaitForMillis(50);
        testSequenceDispatchEventBus.cancelAll();
        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        "3".equals(testSequenceDispatchEventBus.message.toString())));
        assertEquals(1, testSequenceDispatchEventBus.max);
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

    static class TestSequenceDispatchEventBus extends EventBusFixedExecutor {

        private AtomicInteger count = new AtomicInteger(0);
        private int max = 0;
        private StringBuffer message = new StringBuffer();
        private AtomicInteger poolSize = new AtomicInteger(1);

        @AllowConcurrentEvents
        @Subscribe
        public void testMethod(String message) {
            val curr = count.incrementAndGet();
            assertTrue(curr <= poolSize.get());
            synchronized (this) {
                max = Math.max(max, curr);
            }
            awaitForMillis(100);
            this.message.append(message);
            count.decrementAndGet();
        }
    }
}
