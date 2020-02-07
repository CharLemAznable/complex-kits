package com.github.charlemaznable.core.net.ohclient.config;

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
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OhConfigContentFormat {

    Class<? extends ContentFormat> value();

    interface ContentFormat {

        String contentType();

        String format(@Nonnull final Map<String, Object> parameterMap,
                      @Nonnull final Map<String, Object> contextMap);
    }

    class FormContentFormat implements ContentFormat {

        @Override
        public String contentType() {
            return APPLICATION_FORM_URLENCODED_VALUE;
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

    class JsonContentFormat implements ContentFormat {

        @Override
        public String contentType() {
            return APPLICATION_JSON_VALUE;
        }

        @Override
        public String format(@Nonnull final Map<String, Object> parameterMap,
                             @Nonnull final Map<String, Object> contextMap) {
            return json(newHashMap(parameterMap));
        }
    }

    abstract class XmlContentFormat implements ContentFormat {

        public static final String XML_ROOT_NAME = "XML_ROOT_NAME";

        @Override
        public String format(@Nonnull final Map<String, Object> parameterMap,
                             @Nonnull final Map<String, Object> contextMap) {
            return xml(newHashMap(parameterMap), getStr(contextMap, XML_ROOT_NAME, "xml"));
        }
    }

    class ApplicationXmlContentFormat extends XmlContentFormat {

        @Override
        public String contentType() {
            return APPLICATION_XML_VALUE;
        }
    }

    class TextXmlContentFormat extends XmlContentFormat {

        @Override
        public String contentType() {
            return TEXT_XML_VALUE;
        }
    }
}
