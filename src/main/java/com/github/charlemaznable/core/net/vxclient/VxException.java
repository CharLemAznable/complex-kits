package com.github.charlemaznable.core.net.vxclient;

public final class VxException extends RuntimeException {

    private static final long serialVersionUID = -4064920583673899451L;

    public VxException(String msg) {
        super(msg);
    }

    public VxException(Throwable cause) {
        super(cause);
    }
}
