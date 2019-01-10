package com.github.charlemaznable.net;

import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.net.URLEncoder;

import static org.apache.commons.lang3.StringUtils.replace;

public class Url {

    public static final String UTF_8 = "UTF-8";

    @SneakyThrows
    public static String encode(String s) {
        return URLEncoder.encode(s, UTF_8);
    }

    @SneakyThrows
    public static String decode(String s) {
        return URLDecoder.decode(s, UTF_8);
    }

    public static String encodeDotAndColon(String s) {
        return replace(replace(s, ".", "-"), ":", "_");
    }

    public static String decodeDotAndColon(String s) {
        return replace(replace(s, "-", "."), "_", ":");
    }
}
