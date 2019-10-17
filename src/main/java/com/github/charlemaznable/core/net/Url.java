package com.github.charlemaznable.core.net;

import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.replace;

public class Url {

    private Url() {}

    @SneakyThrows
    public static String encode(String s) {
        return URLEncoder.encode(s, UTF_8.name());
    }

    @SneakyThrows
    public static String decode(String s) {
        return URLDecoder.decode(s, UTF_8.name());
    }

    public static String encodeDotAndColon(String s) {
        return replace(replace(s, ".", "-"), ":", "_");
    }

    public static String decodeDotAndColon(String s) {
        return replace(replace(s, "-", "."), "_", ":");
    }
}
