package com.github.charlemaznable.core.config.impl;

import lombok.val;
import lombok.var;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public class CSVLineReader {

    /**
     * The default separator to use if none is supplied to the constructor.
     */
    public static final char DEFAULT_SEPARATOR = ',';

    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';

    private static final char SEPARATOR = DEFAULT_SEPARATOR; // 分隔符

    private static final char QUOTECHAR = DEFAULT_QUOTE_CHARACTER; // 引号符

    public String[] parseLine(String line) {
        List<String> tokensOnThisLine = newArrayList();
        var sb = new StringBuilder();
        var inQuotes = false;
        var skipNext = false;
        do {
            for (var i = 0; i < line.length(); i++) {
                if (skipNext) {
                    skipNext = false;
                    continue;
                }
                val c = line.charAt(i);
                if (c == QUOTECHAR) {
                    val res = parseInQuotes(inQuotes, line, i, sb);
                    inQuotes = res.getLeft();
                    skipNext = res.getRight();
                } else if (c == SEPARATOR && !inQuotes) {
                    tokensOnThisLine.add(sb.toString().trim());
                    sb = new StringBuilder(); // start work on next token
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);

        tokensOnThisLine.add(sb.toString().trim());
        return tokensOnThisLine.toArray(new String[0]);
    }

    @SuppressWarnings("Duplicates")
    static Pair<Boolean/* inQuotes */, Boolean/* skipNext */> parseInQuotes(
            boolean inQuotes, String line, int i, StringBuilder sb) {
        // this gets complex... the quote may end a quoted block, or
        // escape another quote. do a 1-char lookahead:
        if (inQuotes // we are in quotes, therefore there can be
                // escaped quotes in here.
                && line.length() > i + 1 // there is indeed
                // another character to check.
                && line.charAt(i + 1) == QUOTECHAR) { // ..and
            // that char. is a quote also.
            // we have two quote chars in a row == one quote char,
            // so consume them both and
            // put one on the token. we do *not* exit the quoted text.
            sb.append(line.charAt(i + 1));
            return Pair.of(true, true);
        } else {
            // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
            if (i > 2 // not on the begining of the line
                    && line.charAt(i - 1) != SEPARATOR // not at the  begining of an escape sequence
                    && line.length() > i + 1
                    && line.charAt(i + 1) != SEPARATOR // not at the end of an escape sequence
                    ) {
                sb.append(QUOTECHAR);
            }
            return Pair.of(!inQuotes, false);
        }
    }
}
