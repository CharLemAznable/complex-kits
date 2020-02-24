package com.github.charlemaznable.core.miner.testClass;

import com.github.charlemaznable.core.miner.MinerConfig;

@MinerConfig("DEFAULT_DATA")
public interface TestMiner {

    String name();

    String full();

    @MinerConfig(dataIdProvider = TestMinerDataId.class)
    String longName();

    @MinerConfig(dataIdProvider = TestMinerDataIdProvider.class)
    String longWrap();

    String abc(String defaultValue);
}
