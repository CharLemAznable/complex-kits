package com.github.charlemaznable.core.lang.concurrent;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static java.lang.Runtime.getRuntime;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public abstract class EventBusExecutor {

    private static final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(getRuntime().availableProcessors() + 1);

    private Object subscriber;
    private Executor executor;
    private AsyncEventBus eventBus;

    public EventBusExecutor() {
        this(null);
    }

    public EventBusExecutor(Object subscriber) {
        this.subscriber = subscriber;
    }

    public final void post(Object event) {
        post(event, 0, NANOSECONDS);
    }

    public final void post(Object event, long delay, TimeUnit unit) {
        init();
        configExecutor(executor);
        scheduler.schedule(() -> eventBus.post(event), delay, unit);
    }

    protected void configExecutor(Executor executor) {}

    protected String eventBusIdentifier() {
        return getClass().getName();
    }

    protected abstract Executor eventBusExecutor();

    private synchronized void init() {
        if (nonNull(this.eventBus)) return;

        this.executor = eventBusExecutor();
        this.eventBus = new AsyncEventBus(eventBusIdentifier(), executor);
        this.eventBus.register(nullThen(this.subscriber, () -> this));
    }
}
