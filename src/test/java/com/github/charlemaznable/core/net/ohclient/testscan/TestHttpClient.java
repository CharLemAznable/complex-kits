package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;

@OhClient("${root}:41102")
public interface TestHttpClient {

    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }
}
