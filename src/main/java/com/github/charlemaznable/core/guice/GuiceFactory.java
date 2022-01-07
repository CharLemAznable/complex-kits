package com.github.charlemaznable.core.guice;

import com.github.charlemaznable.core.lang.Factory;
import com.google.inject.Injector;
import lombok.AllArgsConstructor;

import static com.github.charlemaznable.core.lang.Clz.isConcrete;
import static org.joor.Reflect.onClass;

@AllArgsConstructor
public final class GuiceFactory implements Factory {

    private Injector injector;

    @Override
    public <T> T build(Class<T> clazz) {
        try {
            return injector.getInstance(clazz);
        } catch (Exception e) {
            if (!isConcrete(clazz)) return null;
            return onClass(clazz).create().get();
        }
    }
}
