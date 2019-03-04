package com.github.charlemaznable.lang;

import lombok.val;
import lombok.var;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.repeat;

public class Str {

    /**
     * 整数匹配模式.
     */
    public static final Pattern INTEGER_PATTERN = compile("[-+]?([0-9]+)$");

    public static boolean isNull(String str) {
        return str == null;
    }

    public static boolean isNotNull(String str) {
        return str != null;
    }

    public static boolean isEmpty(String str) {
        return isNull(str) || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return isNotNull(str) && !str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return isEmpty(str) || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return isNotEmpty(str) && !str.trim().isEmpty();
    }

    public static String padding(String s, char letter, int repeats) {
        val sb = new StringBuilder(s);
        while (repeats-- > 0) {
            sb.append(letter);
        }

        return sb.toString();
    }

    public static String removeLastLetters(String s, char letter) {
        val sb = new StringBuilder(s);
        while (sb.charAt(sb.length() - 1) == letter)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    // return true if 'left' and 'right' are matching parens/brackets/braces
    public static boolean matches(char left, char right) {
        if (left == '(') return right == ')';
        if (left == '[') return right == ']';
        return left == '{' && right == '}';
    }

    @SuppressWarnings("Duplicates")
    public static String substrInQuotes(String str, char left, int pos) {
        var leftTimes = 0;
        val leftPos = str.indexOf(left, pos);
        if (leftPos < 0) return "";

        for (var i = leftPos + 1; i < str.length(); ++i) {
            val charAt = str.charAt(i);
            if (charAt == left) ++leftTimes;
            else if (matches(left, charAt)) {
                if (leftTimes == 0) return str.substring(leftPos + 1, i);
                --leftTimes;
            }
        }

        return "";
    }

    public static String toStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 判断字符串是否整数。
     *
     * @param string 字符串。
     * @return true 是整数。
     */
    public static boolean isInteger(String string) {
        val matcher = INTEGER_PATTERN.matcher(string);
        if (!matcher.matches()) return false;

        val number = matcher.group(1);
        val maxValue = "" + Integer.MAX_VALUE;
        return number.length() <= maxValue.length() &&
                alignRight(number, maxValue.length(), '0').compareTo(maxValue) <= 0;

    }

    /**
     * 判断字符串是否长整数。
     *
     * @param string 字符串。
     * @return true 是长整数。
     */
    public static boolean isLong(String string) {
        val matcher = INTEGER_PATTERN.matcher(string);
        if (!matcher.matches()) return false;

        val number = matcher.group(1);
        val maxValue = "" + Long.MAX_VALUE;
        return number.length() <= maxValue.length() &&
                alignRight(number, maxValue.length(), '0').compareTo(maxValue) <= 0;

    }

    /**
     * 在字符串左侧填充一定数量的特殊字符.
     *
     * @param cs    字符串
     * @param width 字符数量
     * @param c     字符
     * @return 新字符串
     */
    public static String alignRight(CharSequence cs, int width, char c) {
        if (null == cs) return null;
        val len = cs.length();
        if (len >= width) return cs.toString();
        return repeat(c, width - len) + cs;
    }

    public static Integer intOf(String str) {
        return intOf(str, 0);
    }

    public static Integer intOf(String str, Integer defaultValue) {
        return intOf(str, 10, defaultValue);
    }

    public static Integer intOf(String str, int radix, Integer defaultValue) {
        try {
            return Integer.parseInt(str, radix);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Long longOf(String str) {
        return longOf(str, 0L);
    }

    public static Long longOf(String str, Long defaultValue) {
        return longOf(str, 10, 0L);
    }

    public static Long longOf(String str, int radix, Long defaultValue) {
        try {
            return Long.parseLong(str, radix);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Short shortOf(String str) {
        return shortOf(str, (short) 0);
    }

    public static Short shortOf(String str, Short defaultValue) {
        return shortOf(str, 10, (short) 0);
    }

    public static Short shortOf(String str, int radix, Short defaultValue) {
        try {
            return Short.parseShort(str, radix);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Float floatOf(String str) {
        return floatOf(str, 0F);
    }

    public static Float floatOf(String str, Float defaultValue) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Double doubleOf(String str) {
        return doubleOf(str, 0D);
    }

    public static Double doubleOf(String str, Double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
