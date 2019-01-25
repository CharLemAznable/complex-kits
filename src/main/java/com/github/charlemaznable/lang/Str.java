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

    public static StringBuilder padding(String s, char letter, int repeats) {
        val sb = new StringBuilder(s);
        while (repeats-- > 0) {
            sb.append(letter);
        }

        return sb;
    }

    public static StringBuilder removeLastLetters(String s, char letter) {
        val sb = new StringBuilder(s);
        while (sb.charAt(sb.length() - 1) == letter)
            sb.deleteCharAt(sb.length() - 1);

        return sb;
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
}
