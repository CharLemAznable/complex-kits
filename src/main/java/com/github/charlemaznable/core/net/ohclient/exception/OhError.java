package com.github.charlemaznable.core.net.ohclient.exception;

import lombok.Getter;

@Getter
public class OhError extends RuntimeException {

    private static final long serialVersionUID = 3181836259126302177L;
    private final int statusCode;

    public OhError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
