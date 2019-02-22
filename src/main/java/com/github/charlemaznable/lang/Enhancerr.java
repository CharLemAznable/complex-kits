package com.github.charlemaznable.lang;

import lombok.val;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

import static com.github.charlemaznable.lang.Clz.getConstructorParameterTypes;
import static com.github.charlemaznable.lang.Condition.checkNotNull;

public class Enhancerr {

    public static Object create
            (Class type, Callback callback, Object[] arguments) {
        val argumentTypes = getConstructorParameterTypes(type, arguments);
        checkNotNull(argumentTypes, new IllegalArgumentException(
                "Constructor with such arguments Not Found"));

        return create(type, callback, argumentTypes, arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, Callback callback, Object[] arguments) {
        val argumentTypes = getConstructorParameterTypes(superclass, arguments);
        checkNotNull(argumentTypes, new IllegalArgumentException(
                "Constructor with such arguments Not Found"));

        return create(superclass, interfaces, callback, argumentTypes, arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks, Object[] arguments) {
        val argumentTypes = getConstructorParameterTypes(superclass, arguments);
        checkNotNull(argumentTypes, new IllegalArgumentException(
                "Constructor with such arguments Not Found"));

        return create(superclass, interfaces, filter, callbacks, argumentTypes, arguments);
    }

    public static Object create
            (Class type, Callback callback, Class[] argumentTypes, Object[] arguments) {
        val e = new Enhancer();
        e.setSuperclass(type);
        e.setCallback(callback);
        return e.create(argumentTypes, arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, Callback callback, Class[] argumentTypes, Object[] arguments) {
        val e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallback(callback);
        return e.create(argumentTypes, arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks, Class[] argumentTypes, Object[] arguments) {
        val e = new Enhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(filter);
        e.setCallbacks(callbacks);
        return e.create(argumentTypes, arguments);
    }
}
