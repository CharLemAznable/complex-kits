package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;

@TestClientMapping
@OhClient
public interface TestHttpClient {

    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }
}
