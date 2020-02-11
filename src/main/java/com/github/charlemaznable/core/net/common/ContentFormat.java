package com.github.charlemaznable.core.net.common;

import com.github.charlemaznable.core.net.Url;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Xml.xml;
import static com.github.charlemaznable.core.lang.Mapp.getStr;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.google.common.net.MediaType.APPLICATION_XML_UTF_8;
import static com.google.common.net.MediaType.FORM_DATA;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.XML_UTF_8;
import static java.util.Objects.nonNull;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentFormat {

    Class<? extends ContentFormatter> value();

    interface ContentFormatter {

        String contentType();

        String format(@Nonnull final Map<String, Object> parameterMap,
                      @Nonnull final Map<String, Object> contextMap);
    }

    class FormContentFormatter implements ContentFormatter {

        @Override
        public String contentType() {
            return FORM_DATA.toString();
        }

        @Override
        public String format(@Nonnull final Map<String, Object> parameterMap,
                             @Nonnull final Map<String, Object> contextMap) {
            return newHashMap(parameterMap).entrySet().stream()
                    .filter(e -> isNotBlank(e.getKey()) && nonNull(e.getValue()))
                    .map(e -> e.getKey() + "=" + Url.encode(toStr(e.getValue())))
                    .collect(Collectors.joining("&"));
        }
    }

    class JsonContentFormatter implements ContentFormatter {

        @Override
        public String contentType() {
            return JSON_UTF_8.toString();
        }

        @Override
        public String format(@Nonnull final Map<String, Object> parameterMap,
                             @Nonnull final Map<String, Object> contextMap) {
            return json(newHashMap(parameterMap));
        }
    }

    abstract class XmlContentFormatter implements ContentFormatter {

        public static final String XML_ROOT_NAME = "XML_ROOT_NAME";

        @Override
        public String format(@Nonnull final Map<String, Object> parameterMap,
                             @Nonnull final Map<String, Object> contextMap) {
            return xml(newHashMap(parameterMap), getStr(contextMap, XML_ROOT_NAME, "xml"));
        }
    }

    class ApplicationXmlContentFormatter extends XmlContentFormatter {

        @Override
        public String contentType() {
            return APPLICATION_XML_UTF_8.toString();
        }
    }

    class TextXmlContentFormatter extends XmlContentFormatter {

        @Override
        public String contentType() {
            return XML_UTF_8.toString();
        }
    }
}
