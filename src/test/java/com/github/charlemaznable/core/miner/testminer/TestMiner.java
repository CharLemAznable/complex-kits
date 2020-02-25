package com.github.charlemaznable.core.miner.testminer;

import com.github.charlemaznable.core.miner.MinerConfig;

@MinerConfig("DEFAULT_DATA")
public interface TestMiner {

    String name();

    String full();

    @MinerConfig(dataIdProvider = TestMinerDataId.class)
    String longName();

    @MinerConfig(dataIdProvider = TestMinerDataIdWrapper.class)
    String longWrap();

    String abc(String defaultValue);

    @MinerConfig(defaultValueProvider = TestDefaultInContext.class)
    String defaultInContext();
}
