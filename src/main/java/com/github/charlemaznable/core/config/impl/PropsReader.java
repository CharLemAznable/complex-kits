package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ex.ConfigException;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;

public final class PropsReader extends LineNumberReader {

    static final String COMMENT_CHARS = "#!";

    static final String DEFAULT_SEPARATOR = " = ";
    /**
     * The list of possible key/value separators
     */
    private static final char[] SEPARATORS = new char[]{'=', ':'};
    /**
     * The regular expression to parse the key and the value of a property.
     */
    private static final Pattern PROPERTY_PATTERN = compile("(([\\S&&[^\\\\" + new String(SEPARATORS)
            + "]]|\\\\.)*)(\\s*(\\s+|[" + new String(SEPARATORS) + "])\\s*)(.*)");
    /**
     * Constant for the radix of hex numbers.
     */
    private static final int HEX_RADIX = 16;
    /**
     * Constant for the length of a unicode literal.
     */
    private static final int UNICODE_LEN = 4;
    /**
     * Constant for the index of the group for the key.
     */
    private static final int IDX_KEY = 1;
    /**
     * Constant for the index of the group for the value.
     */
    private static final int IDX_VALUE = 5;
    /**
     * Constant for the index of the group for the separator.
     */
    private static final int IDX_SEPARATOR = 3;
    private static final Map<Character, Character> UNESCAPE_SLASH_MAP = of(
            '\\', '\\',
            '\'', '\'',
            '\"', '"',
            'r', '\r',
            'f', '\f',
            't', '\t',
            'n', '\n',
            'b', '\b'
    );
    /**
     * Stores the name of the last read property.
     */
    private String propertyName;
    /**
     * Stores the value of the last read property.
     */
    private String propertyValue;

    public PropsReader(Reader reader) {
        super(reader);
    }

    private static boolean checkCombineLines(String line) {
        return countTrailingBS(line) % 2 != 0;
    }

    private static String[] doParseProperty(String line) {
        val matcher = PROPERTY_PATTERN.matcher(line);

        val result = new String[]{"", "", ""};

        if (matcher.matches()) {
            result[0] = matcher.group(IDX_KEY).trim();
            result[1] = matcher.group(IDX_VALUE).trim();
            result[2] = matcher.group(IDX_SEPARATOR);
        }

        return result;
    }

    static boolean isCommentLine(String line) {
        val s = line.trim();
        // blanc lines are also treated as comment lines
        return s.length() < 1 || COMMENT_CHARS.indexOf(s.charAt(0)) >= 0;
    }

    private static int countTrailingBS(String line) {
        int bsCount = 0;
        for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--)
            bsCount++;

        return bsCount;
    }

    @SuppressWarnings("SameParameterValue")
    protected static String unescapeJava(String str, char delimiter) {
        if (isNull(str)) return null;

        val sz = str.length();
        val out = new StringBuilder(sz);
        val unicode = new StringBuilder(UNICODE_LEN);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            val ch = str.charAt(i);
            if (inUnicode) {
                val res = unescapeUnicode(ch, hadSlash, out, unicode);
                inUnicode = res.getLeft();
                hadSlash = res.getRight();
            } else if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                if (ch == 'u') inUnicode = true; // uh-oh, we're in unicode country....
                else unescapeSlash(ch, out, delimiter);

            } else if (ch == '\\') {
                hadSlash = true;
            } else out.append(ch);
        }

        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.append('\\');
        }

        return out.toString();
    }

    private static Pair<Boolean/* inUnicode */, Boolean/* hadSlash */> unescapeUnicode(
            char ch, boolean hadSlash, StringBuilder out, StringBuilder unicode) {
        // if in unicode, then we're reading unicode
        // values in somehow
        unicode.append(ch);
        if (unicode.length() == UNICODE_LEN) {
            // unicode now contains the four hex digits
            // which represents our unicode character
            try {
                val value = Integer.parseInt(unicode.toString(), HEX_RADIX);
                out.append((char) value);
                unicode.setLength(0);
                return Pair.of(false, false);
            } catch (NumberFormatException nfe) {
                throw new ConfigException("Unable to parse unicode value: " + unicode, nfe);
            }
        }
        return Pair.of(true, hadSlash);
    }

    private static void unescapeSlash(char ch, StringBuilder out, char delimiter) {
        if (nonNull(UNESCAPE_SLASH_MAP.get(ch))) out.append(UNESCAPE_SLASH_MAP.get(ch));
        else if (ch == delimiter) out.append('\\').append(delimiter);
        else out.append(ch);
    }

    public String readProperty() throws IOException {
        val buffer = new StringBuilder();

        while (true) {
            val line = readLine();
            if (isNull(line)) return null; // EOF

            if (!readPropertyLine(line, buffer)) break;
        }

        return buffer.toString();
    }

    private boolean readPropertyLine(String line, StringBuilder buffer) {
        if (isCommentLine(line)) return true;

        line = line.trim();

        if (checkCombineLines(line)) {
            line = line.substring(0, line.length() - 1);
            buffer.append(line);
            return true;
        } else {
            buffer.append(line);
            return false;
        }
    }

    public boolean nextProperty() throws IOException {
        val line = readProperty();

        if (isNull(line)) return false; // EOF

        // parse the line
        parseProperty(line);
        return true;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    protected void parseProperty(String line) {
        val property = doParseProperty(line);
        initPropertyName(property[0]);
        initPropertyValue(property[1]);
    }

    protected void initPropertyName(String name) {
        propertyName = StringEscapeUtils.unescapeJava(name);
    }

    protected void initPropertyValue(String value) {
        propertyValue = unescapeJava(value, ',');
    }
}
