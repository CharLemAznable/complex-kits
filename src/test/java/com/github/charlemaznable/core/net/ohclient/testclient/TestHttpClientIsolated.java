package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.annotation.IsolatedConnectionPool;

@TestClientMapping
@OhClient
@IsolatedConnectionPool
public interface TestHttpClientIsolated {

    @IsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
