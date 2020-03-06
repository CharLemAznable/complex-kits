package com.github.charlemaznable.core.net.common;

public interface CncRequest<T extends CncResponse> {

    Class<? extends T> responseClass();
}
