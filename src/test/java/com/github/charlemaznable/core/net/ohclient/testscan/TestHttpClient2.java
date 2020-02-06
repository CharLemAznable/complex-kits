package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigIsolatedConnectionPool;

@TestClientMapping
@OhClient
@OhConfigIsolatedConnectionPool
public interface TestHttpClient2 {

    @OhConfigIsolatedConnectionPool
    String sample();

    default String sampleWrapper() {
        return "[" + sample() + "]";
    }
}
