package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.miner.MinerConfig;

@MinerConfig("THREAD_DATA")
public interface TestDefaultContext {

    @MinerConfig(defaultValueProvider = TestDefaultContextProvider.class)
    String thread();
}
