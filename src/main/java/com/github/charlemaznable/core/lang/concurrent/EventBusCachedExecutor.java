package com.github.charlemaznable.core.lang.concurrent;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public abstract class EventBusCachedExecutor extends EventBusExecutor {

    public EventBusCachedExecutor() {
        super();
    }

    public EventBusCachedExecutor(Object subscriber) {
        super(subscriber);
    }

    @Override
    public final Executor eventBusExecutor() {
        return newCachedThreadPool();
    }
}
