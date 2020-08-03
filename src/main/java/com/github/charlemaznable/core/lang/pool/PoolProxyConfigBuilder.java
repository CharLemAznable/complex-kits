package com.github.charlemaznable.core.lang.pool;

import lombok.val;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

public final class PoolProxyConfigBuilder {

    private int maxTotal = DEFAULT_MAX_TOTAL;
    private int maxIdle = DEFAULT_MAX_IDLE;
    private int minIdle = DEFAULT_MIN_IDLE;

    public PoolProxyConfigBuilder maxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public PoolProxyConfigBuilder maxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

    public PoolProxyConfigBuilder minIdle(int minIdle) {
        this.minIdle = minIdle;
        return this;
    }

    public <T> GenericObjectPoolConfig<T> build() {
        val config = new GenericObjectPoolConfig<T>();
        config.setMaxTotal(this.maxTotal);
        config.setMaxIdle(this.maxIdle);
        config.setMinIdle(this.minIdle);
        return config;
    }
}
