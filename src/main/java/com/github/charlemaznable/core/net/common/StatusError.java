package com.github.charlemaznable.core.net.common;

import lombok.Getter;

@Getter
public class StatusError extends RuntimeException {

    private static final long serialVersionUID = 3521440029221036523L;
    private final int statusCode;

    public StatusError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
