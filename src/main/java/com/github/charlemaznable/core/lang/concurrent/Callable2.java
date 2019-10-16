package com.github.charlemaznable.core.lang.concurrent;

public interface Callable2<V, T1, T2> {

    V call(T1 param1, T2 param2);
}
