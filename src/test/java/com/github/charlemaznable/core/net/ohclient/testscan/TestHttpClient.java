package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.ohclient.OhMapping;

@TestClientMapping
@OhClient
public interface TestHttpClient {

    @OhMapping(urlProvider = TestSampleUrlProvider.class)
    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }
}
