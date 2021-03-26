package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.ScheduledDispatcherDelegate;
import com.google.common.eventbus.ScheduledEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Await.await;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class EventBusExecutor {

    private ScheduledEventBus eventBus;

    public EventBusExecutor() {
        this(null);
    }

    public EventBusExecutor(Object subscriber) {
        eventBus = new ScheduledEventBus(eventBusIdentifier(), eventBusExecutor());
        eventBus.register(isNull(subscriber) ? this : subscriber);
    }

    public final void post(Object event) {
        post(event, 0, NANOSECONDS);
    }

    public final void post(Object event, long delay, TimeUnit unit) {
        new Thread(() -> {
            await(delay, unit);
            eventBus.post(event);
        }).start();
    }

    public final boolean cancel(Object event) {
        return eventBus.cancel(event);
    }

    public final boolean cancelAll(Object event) {
        return eventBus.cancelAll(event);
    }

    public final void cancelAll() {
        eventBus.cancelAll();
    }

    public boolean suspended() {
        return eventBus.suspended();
    }

    public void suspend() {
        eventBus.suspend();
    }

    public void resume() {
        eventBus.resume();
    }

    @SuppressWarnings("UnusedReturnValue")
    public EventBusExecutor delegate(ScheduledDispatcherDelegate delegate) {
        eventBus.delegate(delegate);
        return this;
    }

    public String eventBusIdentifier() {
        return getClass().getName();
    }

    public abstract Executor eventBusExecutor();
}
