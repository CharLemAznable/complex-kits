package com.github.charlemaznable.core.spring;

public class ActiveProfilesThreadLocal {

    private static ThreadLocal<String[]> local = new ThreadLocal<>();

    private ActiveProfilesThreadLocal() {}

    public static void set(String[] activeProfiles) {
        local.set(activeProfiles);
    }

    public static String[] get() {
        return local.get();
    }

    public static void unload() {
        local.remove();
    }
}
