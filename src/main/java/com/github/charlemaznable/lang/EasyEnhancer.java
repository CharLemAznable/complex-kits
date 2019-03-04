package com.github.charlemaznable.lang;

import lombok.val;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

import static com.github.charlemaznable.lang.Clz.getConstructorParameterTypes;
import static com.github.charlemaznable.lang.Condition.checkNotNull;
import static com.github.charlemaznable.lang.Condition.nullThen;

public class EasyEnhancer extends Enhancer {

    public static Object create
            (Class type, Callback callback,
             Object[] arguments) {
        return create(type, callback,
                constructorParameterTypes(type, arguments), arguments);
    }

    public static Object create
            (Class type, Callback callback,
             Class[] argumentTypes, Object[] arguments) {
        val e = new EasyEnhancer();
        e.setSuperclass(type);
        e.setCallback(callback);
        return e.create(argumentTypes,
                nullThen(arguments, () -> new Object[0]));
    }

    ////////////////////////////////////////////////////////////

    public static Object create
            (Class superclass, Class[] interfaces, Callback callback,
             Object[] arguments) {
        return create(superclass, interfaces, callback,
                constructorParameterTypes(superclass, arguments), arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, Callback callback,
             Class[] argumentTypes, Object[] arguments) {
        val e = new EasyEnhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallback(callback);
        return e.create(argumentTypes,
                nullThen(arguments, () -> new Object[0]));
    }

    ////////////////////////////////////////////////////////////

    public static Object create
            (Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks,
             Object[] arguments) {
        return create(superclass, interfaces, filter, callbacks,
                constructorParameterTypes(superclass, arguments), arguments);
    }

    public static Object create
            (Class superclass, Class[] interfaces, CallbackFilter filter, Callback[] callbacks,
             Class[] argumentTypes, Object[] arguments) {
        val e = new EasyEnhancer();
        e.setSuperclass(superclass);
        e.setInterfaces(interfaces);
        e.setCallbackFilter(filter);
        e.setCallbacks(callbacks);
        return e.create(argumentTypes,
                nullThen(arguments, () -> new Object[0]));
    }

    ////////////////////////////////////////////////////////////

    private static Class<?>[] constructorParameterTypes(Class<?> clazz, Object... arguments) {
        return checkNotNull(getConstructorParameterTypes(clazz, arguments),
                new IllegalArgumentException("Constructor with such arguments Not Found"));
    }
}