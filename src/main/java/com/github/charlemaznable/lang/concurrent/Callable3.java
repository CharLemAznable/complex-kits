package com.github.charlemaznable.lang.concurrent;

public interface Callable3<V, T1, T2, T3> {

    V call(T1 param1, T2 param2, T3 param3) throws Exception;
}
