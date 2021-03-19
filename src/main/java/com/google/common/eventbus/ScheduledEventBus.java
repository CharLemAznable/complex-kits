package com.google.common.eventbus;

import java.util.concurrent.Executor;

import static org.joor.Reflect.on;

public class ScheduledEventBus extends EventBus {

    public ScheduledEventBus(String identifier, Executor executor) {
        super(identifier, executor, new ScheduledDispatcher(executor), LoggingHandler.INSTANCE);
    }

    public ScheduledEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler) {
        super("default", executor, new ScheduledDispatcher(executor), subscriberExceptionHandler);
    }

    public ScheduledEventBus(Executor executor) {
        super("default", executor, new ScheduledDispatcher(executor), LoggingHandler.INSTANCE);
    }

    public boolean remove(Object event) {
        return dispatcher().remove(event);
    }

    public boolean removeAll(Object event) {
        return dispatcher().removeAll(event);
    }

    public boolean suspended() {
        return dispatcher().suspended();
    }

    public void suspend() {
        dispatcher().suspend();
    }

    public void resume() {
        dispatcher().resume();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ScheduledEventBus delegate(ScheduledDispatcherDelegate delegate) {
        dispatcher().delegate(delegate);
        return this;
    }

    private ScheduledDispatcher dispatcher() {
        return on(this).field("dispatcher").get();
    }
}
