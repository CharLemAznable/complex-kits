package com.github.charlemaznable.core.config.impl;

import lombok.val;
import lombok.var;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static java.lang.Character.isWhitespace;
import static java.lang.System.getProperty;

public class IniReader {

    protected static final String COMMENT_CHARS = "#;";

    protected static final String SEPARATOR_CHARS = "=:(";

    private static final String LINE_SEPARATOR = getProperty("line.separator");

    private static final String QUOTE_CHARACTERS = "\"'";

    private static final String LINE_CONT = "\\";

    private Map<String, Properties> properties = newHashMap();

    private List<String> sections = newArrayList();

    private int lineNumber;

    public IniReader(Reader reader) throws IOException {
        sections.add(""); // add Global section.

        val bufferedReader = new BufferedReader(reader);

        var line = bufferedReader.readLine();

        for (; line != null; line = bufferedReader.readLine()) {
            ++lineNumber;
            line = line.trim();
            if (isCommentLine(line)) continue;
            if (isSectionLine(line)) {
                val section = line.substring(1, line.length() - 1).trim();
                if (!sections.contains(section)) sections.add(section);
                continue;
            }
            String key;
            var value = "";
            val index = findSeparator(line);
            if (index >= 0) {
                key = line.substring(0, index);
                value = parseValue(line.substring(index + 1), bufferedReader);

                if (line.charAt(index) == '(' && value.charAt(value.length() - 1) == ')')
                    value = value.substring(0, value.length() - 1);
            } else key = line;
            key = key.trim();
            // use space for properties with no key
            if (key.length() < 1) key = " ";

            createValueNodes(key, value);
        }
    }

    private static String parseValue(String val, BufferedReader reader) throws IOException {
        val propertyValue = new StringBuilder();
        boolean lineContinues;
        var value = val.trim();

        do {
            val quoted = value.startsWith("\"") || value.startsWith("'");
            var stop = false;
            var escape = false;

            val quote = quoted ? value.charAt(0) : 0;

            var i = quoted ? 1 : 0;

            val result = new StringBuilder();
            var lastChar = 0;
            while (i < value.length() && !stop) {
                val c = value.charAt(i);

                if (quoted) {
                    if ('\\' == c && !escape) escape = true;
                    else if (!escape && quote == c) stop = true;
                    else if (escape && quote == c) {
                        escape = false;
                        result.append(c);
                    } else {
                        if (escape) {
                            escape = false;
                            result.append('\\');
                        }

                        result.append(c);
                    }
                } else {
                    if (isCommentChar(c) && isWhitespace(lastChar)) stop = true;
                    else result.append(c);
                }

                i++;
                lastChar = c;
            }

            var v = result.toString();
            if (!quoted) {
                v = v.trim();
                lineContinues = lineContinues(v);
                if (lineContinues) {
                    // remove trailing "\"
                    v = v.substring(0, v.length() - 1).trim();
                }
            } else lineContinues = lineContinues(value, i);
            propertyValue.append(v);

            if (lineContinues) {
                propertyValue.append(LINE_SEPARATOR);
                value = reader.readLine();
            }
        } while (lineContinues && value != null);

        return propertyValue.toString();
    }

    private static boolean lineContinues(String line) {
        val s = line.trim();
        return s.equals(LINE_CONT) || s.length() > 2 && s.endsWith(LINE_CONT)
                && isWhitespace(s.charAt(s.length() - 2));
    }

    private static boolean lineContinues(String line, int pos) {
        String s;

        if (pos >= line.length()) s = line;
        else {
            var end = pos;
            while (end < line.length() && !isCommentChar(line.charAt(end)))
                end++;
            s = line.substring(pos, end);
        }

        return lineContinues(s);
    }

    private static boolean isCommentChar(char c) {
        return COMMENT_CHARS.indexOf(c) >= 0;
    }

    private static int findSeparator(String line) {
        var index1 = findSeparatorBeforeQuote(line,
                findFirstOccurrence(line, QUOTE_CHARACTERS));
        val index2 = findFirstOccurrence(line, SEPARATOR_CHARS);
        if (index1 < 0) index1 = index2;

        return index1 < index2 ? index1 : index2;
    }

    /**
     * Checks for the occurrence of the specified separators in the given line.
     * The index of the first separator is returned.
     *
     * @param line       the line to be investigated
     * @param separators a string with the separator characters to look for
     * @return the lowest index of a separator character or -1 if no separator
     * is found
     */
    private static int findFirstOccurrence(String line, String separators) {
        var index = -1;

        for (var i = 0; i < separators.length(); i++) {
            val sep = separators.charAt(i);
            val pos = line.indexOf(sep);
            if (pos >= 0 && (index < 0 || pos < index)) index = pos;
        }

        return index;
    }

    /**
     * Searches for a separator character directly before a quoting character.
     * If the first non-whitespace character before a quote character is a
     * separator, it is considered the "real" separator in this line - even if
     * there are other separators before.
     *
     * @param line       the line to be investigated
     * @param quoteIndex the index of the quote character
     * @return the index of the separator before the quote or &lt; 0 if there is
     * none
     */
    private static int findSeparatorBeforeQuote(String line, int quoteIndex) {
        var index = quoteIndex - 1;
        while (index >= 0 && isWhitespace(line.charAt(index))) index--;
        if (index >= 0 && SEPARATOR_CHARS.indexOf(line.charAt(index)) < 0) index = -1;

        return index;
    }

    private void createValueNodes(String key, String value) {
        val lastSection = sections.get(sections.size() - 1);
        var sectionProps = properties.get(lastSection);
        if (sectionProps == null) {
            sectionProps = new Properties();
            properties.put(lastSection, sectionProps);
        } else {
            val oldValue = (String) sectionProps.get(key);
            if (oldValue != null)
                putIncKeyAndValue(sectionProps, key, oldValue);
        }

        sectionProps.put(key, value.trim());
    }

    private void putIncKeyAndValue(Properties props, String key, String oldValue) {
        var seq = 0;
        while (props.containsKey(key + "." + seq)) ++seq;

        props.put(key + "." + seq, oldValue);
    }

    /**
     * Determine if the given line is a comment line.
     *
     * @param line The line to check.
     * @return true if the line is empty or starts with one of the comment
     * characters
     */
    protected boolean isCommentLine(String line) {
        if (line == null) return false;
        // blank lines are also treated as comment lines
        return line.length() < 1 || COMMENT_CHARS.indexOf(line.charAt(0)) >= 0;
    }

    /**
     * Determine if the given line is a section.
     *
     * @param line The line to check.
     * @return true if the line contains a section
     */
    protected boolean isSectionLine(String line) {
        return line != null && line.startsWith("[") && line.endsWith("]");
    }

    /**
     * Return a set containing the sections in this ini configuration. Note that
     * changes to this set do not affect the configuration.
     *
     * @return a set containing the sections.
     */
    public List<String> getSections() {
        return sections;
    }

    public Properties getSection(String name) {
        if (name == null) return properties.get("");

        return properties.get(name);
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
