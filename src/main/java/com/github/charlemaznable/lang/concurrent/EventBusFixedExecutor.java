package com.github.charlemaznable.lang.concurrent;

import java.util.concurrent.Executor;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public abstract class EventBusFixedExecutor extends EventBusExecutor {

    public EventBusFixedExecutor() {
        super();
    }

    public EventBusFixedExecutor(Object subscriber) {
        super(subscriber);
    }

    @Override
    public Executor eventBusExecutor() {
        return newFixedThreadPool(threadPoolSize());
    }

    public int threadPoolSize() {
        return getRuntime().availableProcessors() + 1;
    }
}
