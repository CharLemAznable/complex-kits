package com.github.charlemaznable.lang.time;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;

import static com.github.charlemaznable.lang.Condition.checkNotNull;

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

    @CanIgnoreReturnValue
    public String checkFormatQuietly(String dateString) {
        try {
            checkNotNull(dateString);
            return dateString.equals(format.format(
                    format.parse(dateString))) ? dateString : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SneakyThrows
    @CanIgnoreReturnValue
    public String checkFormat(String dateString) {
        checkNotNull(dateString);
        return dateString.equals(format.format(
                format.parse(dateString))) ? dateString : null;
    }
}
