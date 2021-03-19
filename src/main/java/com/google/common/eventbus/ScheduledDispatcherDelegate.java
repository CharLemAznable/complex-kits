package com.google.common.eventbus;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface ScheduledDispatcherDelegate {

    default void configExecutorBeforeDispatch(@Nonnull Executor executor) {}

    default long schedulePeriod() {
        return 0L;
    }

    default TimeUnit schedulePeriodUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
