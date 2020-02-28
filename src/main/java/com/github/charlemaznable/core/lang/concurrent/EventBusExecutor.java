package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class EventBusExecutor {

    private AsyncEventBus eventBus;
    private ScheduledThreadPoolExecutor executor;

    public EventBusExecutor() {
        this(null);
    }

    public EventBusExecutor(Object subscriber) {
        eventBus = new AsyncEventBus(eventBusIdentifier(), eventBusExecutor());
        eventBus.register(isNull(subscriber) ? this : subscriber);

        executor = new ScheduledThreadPoolExecutor(getRuntime().availableProcessors() + 1);
    }

    public final void post(Object event) {
        post(event, 0, NANOSECONDS);
    }

    public final void post(Object event, long delay, TimeUnit unit) {
        executor.schedule(() -> eventBus.post(event), delay, unit);
    }

    public String eventBusIdentifier() {
        return getClass().getName();
    }

    public abstract Executor eventBusExecutor();
}
