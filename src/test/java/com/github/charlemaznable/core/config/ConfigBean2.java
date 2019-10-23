package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.impl.AfterPropertiesSet;
import lombok.Getter;
import lombok.Setter;

import static com.github.charlemaznable.core.lang.Str.isEmpty;

@Getter
@Setter
public class ConfigBean2 implements AfterPropertiesSet {

    private String key1;
    private String key2;
    private String key3;

    @Override
    public void afterPropertiesSet() {
        if (isEmpty(key3)) key3 = key1 + key2;
    }
}
