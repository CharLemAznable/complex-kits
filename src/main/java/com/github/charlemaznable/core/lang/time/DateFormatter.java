package com.github.charlemaznable.core.lang.time;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.SneakyThrows;
import lombok.val;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static java.util.Objects.isNull;

public final class DateFormatter {

    private SimpleDateFormat format;

    public DateFormatter(String pattern) {
        this.format = new SimpleDateFormat(pattern);
    }

    public String transToFormat(String dateString, String toPattern) {
        val parsed = parse(dateString);
        if (isNull(parsed)) return null;
        return new SimpleDateFormat(toPattern).format(parse(dateString));
    }

    public String transFromFormat(String dateString, String fromPattern) {
        try {
            return format(new SimpleDateFormat(fromPattern).parse(dateString));
        } catch (Exception e) {
            return null;
        }
    }

    public String format(Date date) {
        return format.format(date);
    }

    public Date parse(String dateString) {
        try {
            return format.parse(dateString);
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
