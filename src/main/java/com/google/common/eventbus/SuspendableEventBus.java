package com.google.common.eventbus;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static org.joor.Reflect.on;

public class SuspendableEventBus extends EventBus {

    public SuspendableEventBus(String identifier, Executor executor) {
        super(identifier, executor, new SuspendableDispatcher(executor), LoggingHandler.INSTANCE);
    }

    public SuspendableEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler) {
        super("default", executor, new SuspendableDispatcher(executor), subscriberExceptionHandler);
    }

    public SuspendableEventBus(Executor executor) {
        super("default", executor, new SuspendableDispatcher(executor), LoggingHandler.INSTANCE);
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
    public SuspendableEventBus periodSupplier(LongSupplier periodSupplier) {
        dispatcher().periodSupplier(periodSupplier);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SuspendableEventBus unitSupplier(Supplier<TimeUnit> unitSupplier) {
        dispatcher().unitSupplier(unitSupplier);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SuspendableEventBus executorConfiger(Consumer<Executor> executorConfiger) {
        dispatcher().executorConfiger(executorConfiger);
        return this;
    }

    private SuspendableDispatcher dispatcher() {
        return on(this).field("dispatcher").get();
    }
}
