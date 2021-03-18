package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.SuspendableEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static java.lang.Runtime.getRuntime;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class EventBusExecutor {

    private SuspendableEventBus eventBus;
    private ScheduledThreadPoolExecutor executor;

    public EventBusExecutor() {
        this(null);
    }

    public EventBusExecutor(Object subscriber) {
        eventBus = new SuspendableEventBus(eventBusIdentifier(), eventBusExecutor());
        eventBus.register(isNull(subscriber) ? this : subscriber);

        executor = new ScheduledThreadPoolExecutor(getRuntime().availableProcessors() + 1);
    }

    public final void post(Object event) {
        post(event, 0, NANOSECONDS);
    }

    public final void post(Object event, long delay, TimeUnit unit) {
        executor.schedule(() -> eventBus.post(event), delay, unit);
    }

    public final boolean remove(Object event) {
        return eventBus.remove(event);
    }

    public final boolean removeAll(Object event) {
        return eventBus.removeAll(event);
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
    public EventBusExecutor periodSupplier(LongSupplier periodSupplier) {
        eventBus.periodSupplier(periodSupplier);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EventBusExecutor unitSupplier(Supplier<TimeUnit> unitSupplier) {
        eventBus.unitSupplier(unitSupplier);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EventBusExecutor executorConfiger(Consumer<Executor> executorConfiger) {
        eventBus.executorConfiger(executorConfiger);
        return this;
    }

    public String eventBusIdentifier() {
        return getClass().getName();
    }

    public abstract Executor eventBusExecutor();
}
