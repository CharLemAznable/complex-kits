package com.github.charlemaznable.spring;

public class ActiveProfilesThreadLocal {

    private static ThreadLocal<String[]> local = new ThreadLocal<>();

    public static void set(String[] activeProfiles) {
        local.set(activeProfiles);
    }

    public static String[] get() {
        return local.get();
    }
}
