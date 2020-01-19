package com.github.charlemaznable.core.config.ex;

public final class ConfigNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -4768838575212716181L;

    public ConfigNotFoundException(String msg) {
        super(msg);
    }
}
