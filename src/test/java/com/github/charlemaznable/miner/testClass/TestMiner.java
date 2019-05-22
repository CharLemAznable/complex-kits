package com.github.charlemaznable.miner.testClass;

import com.github.charlemaznable.miner.MinerConfig;

@MinerConfig("DEFAULT_DATA")
public interface TestMiner {

    String name();

    String full();

    @MinerConfig("long")
    String longName();

    String abc(String defaultValue);
}
