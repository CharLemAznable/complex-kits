package com.google.common.eventbus;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SuspendableDispatcher extends Dispatcher {

    private final ConcurrentLinkedQueue<EventWithSubscriber> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean suspended = new AtomicBoolean(false);
    private final AsyncEventBus poller = new AsyncEventBus(Executors.newCachedThreadPool());
    private final PollEvent pollEvent = new PollEvent() {};
    private final ScheduledExecutorService delayer = Executors.newSingleThreadScheduledExecutor();
    @Setter
    @Accessors(fluent = true)
    private Supplier<Long> periodSupplier = () -> 100L;
    @Setter
    @Accessors(fluent = true)
    private Supplier<TimeUnit> unitSupplier = () -> TimeUnit.MILLISECONDS;

    SuspendableDispatcher() {
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
        try {
            if (suspended()) return;
            EventWithSubscriber e;
            if ((e = queue.poll()) != null) {
                e.subscriber.dispatchEvent(e.event);
            }
        } finally {
            schedule();
        }
    }

    private void schedule() {
        delayer.schedule(() -> poller.post(pollEvent),
                periodSupplier.get(), unitSupplier.get());
    }

    private interface PollEvent {}

    @AllArgsConstructor
    private static final class EventWithSubscriber {

        private final Object event;
        private final Subscriber subscriber;
    }
}
