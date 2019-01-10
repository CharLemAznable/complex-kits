package com.github.charlemaznable.lang.concurrent;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public abstract class EventBusCachedExecutor extends EventBusExecutor {

    @Override
    public Executor eventBusExecutor() {
        return newCachedThreadPool();
    }
}
