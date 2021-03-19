package com.google.common.eventbus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.notNullThenRun;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SuspendableDispatcher extends Dispatcher {

    private final Executor executor;
    private final ConcurrentLinkedQueue<EventWithSubscriber> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean suspended = new AtomicBoolean(false);
    private final AsyncEventBus poller = new AsyncEventBus(Executors.newCachedThreadPool());
    private final PollEvent pollEvent = new PollEvent() {};
    private final ScheduledExecutorService delayer = Executors.newSingleThreadScheduledExecutor();
    @Setter
    @Accessors(fluent = true)
    private LongSupplier periodSupplier = () -> 0L;
    @Setter
    @Accessors(fluent = true)
    private Supplier<TimeUnit> unitSupplier = () -> TimeUnit.MILLISECONDS;
    @Setter
    @Accessors(fluent = true)
    private Consumer<Executor> executorConfiger = e -> {};

    SuspendableDispatcher(Executor executor) {
        this.executor = checkNotNull(executor);
        poller.register(this);
        schedule();
    }

    @Override
    void dispatch(Object event, Iterator<Subscriber> subscribers) {
        checkNotNull(event);
        while (subscribers.hasNext()) {
            queue.add(new EventWithSubscriber(event, subscribers.next()));
        }
    }

    boolean remove(Object event) {
        checkNotNull(event);
        return queue.remove(new EventWithSubscriber(event, null));
    }

    boolean removeAll(Object event) {
        checkNotNull(event);
        return queue.removeIf(e -> event.equals(e.event));
    }

    boolean suspended() {
        return suspended.get();
    }

    void suspend() {
        suspended.set(true);
    }

    void resume() {
        suspended.set(false);
    }

    @Subscribe
    private void pollDispatch(PollEvent event) {
        executorConfiger.accept(executor);
        executor.execute(() -> {
            try {
                if (suspended()) return;
                notNullThenRun(queue.poll(), e ->
                        e.subscriber.dispatchEvent(e.event));
            } finally {
                schedule();
            }
        });
    }

    private void schedule() {
        delayer.schedule(() -> poller.post(pollEvent),
                periodSupplier.getAsLong(), unitSupplier.get());
    }

    private interface PollEvent {}

    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class EventWithSubscriber {

        private final Object event;
        @EqualsAndHashCode.Exclude
        private final Subscriber subscriber;
    }
}
