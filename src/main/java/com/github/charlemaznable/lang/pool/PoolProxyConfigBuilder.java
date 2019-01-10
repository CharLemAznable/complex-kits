package com.github.charlemaznable.lang.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class PoolProxyConfigBuilder {

    private int maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
    private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
    private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

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
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(this.maxTotal);
        config.setMaxIdle(this.maxIdle);
        config.setMinIdle(this.minIdle);
        return config;
    }
}
