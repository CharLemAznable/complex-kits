package com.github.charlemaznable.core.codec.signature;

import com.github.charlemaznable.core.codec.Digest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.github.charlemaznable.core.lang.Str.isNotEmpty;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public class SignatureOptions {

    private static final String DEFAULT_KEY = "signature";
    private static final boolean DEFAULT_FLAT_VALUE = true;
    private static final boolean DEFAULT_KEY_SORT_ASC = true;
    private static final Predicate<Map.Entry<String, String>> DEFAULT_ENTRY_FILTER
            = e -> isNotEmpty(e.getKey()) && isNotEmpty(e.getValue());
    private static final Function<Map.Entry<String, String>, String> DEFAULT_ENTRY_MAPPER
            = e -> e.getKey() + "=" + e.getValue();
    private static final String DEFAULT_ENTRY_SEPARATOR = "&";
    private static final UnaryOperator<String> DEFAULT_PLAIN_PROCESSOR
            = plain -> plain;
    private static final UnaryOperator<String> DEFAULT_SIGN_ALGORITHM
            = Digest.SHA256::digestBase64;
    private static final BiPredicate<String, String> DEFAULT_VERIFY_ALGORITHM
            = (plain, sign) -> Digest.SHA256.digestBase64(plain).equals(sign);

    private String key = DEFAULT_KEY;
    private boolean flatValue = DEFAULT_FLAT_VALUE;
    private boolean keySortAsc = DEFAULT_KEY_SORT_ASC;
    private Predicate<Map.Entry<String, String>> entryFilter = DEFAULT_ENTRY_FILTER;
    private Function<Map.Entry<String, String>, String> entryMapper = DEFAULT_ENTRY_MAPPER;
    private String entrySeparator = DEFAULT_ENTRY_SEPARATOR;
    private UnaryOperator<String> plainProcessor = DEFAULT_PLAIN_PROCESSOR;
    private UnaryOperator<String> signAlgorithm = DEFAULT_SIGN_ALGORITHM;
    private BiPredicate<String, String> verifyAlgorithm = DEFAULT_VERIFY_ALGORITHM;
}
