package com.github.charlemaznable.core.config.spring;

import com.github.charlemaznable.core.config.Configable;
import com.github.charlemaznable.core.config.EnvConfig;

@EnvConfig
public interface TestEnvSpringConfig extends Configable {

    String key1();

    String key2();

    String key3();

    @EnvConfig("testIni.key4")
    String key4();

    String key5();

    @EnvConfig(configKey = "key5", defaultValue = "value5")
    String key5Def();

    String key5(String defaultValue);

    @EnvConfig("custom1.key1")
    String custom1Key1();

    @EnvConfig("custom1.key2")
    String custom1Key2();

    @EnvConfig("custom2.key1")
    String custom2Key1();

    @EnvConfig("custom2.key2")
    String custom2Key2();
}
