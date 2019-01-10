package com.github.charlemaznable.lang.time;

import java.text.SimpleDateFormat;

public class DateFormatter {

    private SimpleDateFormat format;

    public DateFormatter(String pattern) {
        this.format = new SimpleDateFormat(pattern);
    }

    public String transToFormat(String dateString, String toPattern) {
        try {
            return new SimpleDateFormat(toPattern).format(this.format.parse(dateString));
        } catch (Exception e) {
            return null;
        }
    }

    public String transFromFormat(String dateString, String fromPattern) {
        try {
            return format.format(new SimpleDateFormat(fromPattern).parse(dateString));
        } catch (Exception e) {
            return null;
        }
    }
}
