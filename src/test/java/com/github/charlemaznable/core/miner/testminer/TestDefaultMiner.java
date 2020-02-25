package com.github.charlemaznable.core.miner.testminer;

import com.github.charlemaznable.core.miner.MinerConfig;

@MinerConfig("THREAD_DATA")
public interface TestDefaultMiner {

    @MinerConfig(defaultValueProvider = TestDefaultMinerProvider.class)
    String thread();
}
