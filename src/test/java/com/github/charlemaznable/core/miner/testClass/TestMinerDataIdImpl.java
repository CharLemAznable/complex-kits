package com.github.charlemaznable.core.miner.testClass;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class TestMinerDataIdImpl implements TestMinerDataId {

    @Getter
    private String dataId = "long";
}
