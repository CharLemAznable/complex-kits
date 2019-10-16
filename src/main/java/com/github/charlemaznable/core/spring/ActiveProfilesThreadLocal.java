package com.github.charlemaznable.core.spring;

public class ActiveProfilesThreadLocal {

    private static ThreadLocal<String[]> local = new ThreadLocal<>();

    public static void set(String[] activeProfiles) {
        local.set(activeProfiles);
    }

    public static String[] get() {
        return local.get();
    }

    public void unload() {
        local.remove();
    }
}
