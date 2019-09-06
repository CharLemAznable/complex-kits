package com.github.charlemaznable.core.miner;

public class MinerConfigException extends RuntimeException {

    private static final long serialVersionUID = -1468891602796981081L;

    public MinerConfigException(String msg) {
        super(msg);
    }

    public MinerConfigException(String msg, Throwable e) {
        super(msg, e);
    }

    public MinerConfigException(Throwable e) {
        super(e);
    }
}
