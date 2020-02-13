package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.annotation.IsolatedConnectionPool;

@TestClientMapping
@OhClient
@IsolatedConnectionPool
public interface TestHttpClient2 {

    @IsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
