package com.github.charlemaznable.core.miner.testClass;

import com.github.charlemaznable.core.miner.MinerConfig;

@MinerConfig("DEFAULT_DATA")
public interface TestMiner {

    String name();

    String full();

    @MinerConfig("long")
    String longName();

    String abc(String defaultValue);
}
