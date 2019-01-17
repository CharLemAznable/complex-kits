package com.github.charlemaznable.lang.time;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Calendar.getInstance;

public class Now {

    public static String now() {
        return now("yyyy-MM-dd HH:mm:ss");
    }

    public static String millis() {
        return now("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String now(String format) {
        return new SimpleDateFormat(format).format(date());
    }

    public static Date date() {
        return getInstance().getTime();
    }
}
