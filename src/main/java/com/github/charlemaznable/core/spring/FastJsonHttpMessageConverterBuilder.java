package com.github.charlemaznable.core.spring;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.val;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class FastJsonHttpMessageConverterBuilder {

    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public FastJsonHttpMessageConverterBuilder charset(Charset charset) {
        this.fastJsonConfig.setCharset(charset);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setSerializeConfig(SerializeConfig serializeConfig) {
        this.fastJsonConfig.setSerializeConfig(serializeConfig);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setParserConfig(ParserConfig parserConfig) {
        this.fastJsonConfig.setParserConfig(parserConfig);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setParseProcess(ParseProcess parseProcess) {
        this.fastJsonConfig.setParseProcess(parseProcess);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setSerializerFeatures(SerializerFeature... serializerFeatures) {
        this.fastJsonConfig.setSerializerFeatures(serializerFeatures);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setSerializeFilters(SerializeFilter... serializeFilters) {
        this.fastJsonConfig.setSerializeFilters(serializeFilters);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setFeatures(Feature... features) {
        this.fastJsonConfig.setFeatures(features);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setClassSerializeFilters(Map<Class<?>, SerializeFilter> classSerializeFilters) {
        this.fastJsonConfig.setClassSerializeFilters(classSerializeFilters);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder dateFormat(String dateFormat) {
        this.fastJsonConfig.setDateFormat(dateFormat);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder setWriteContentLength(boolean writeContentLength) {
        this.fastJsonConfig.setWriteContentLength(writeContentLength);
        return this;
    }

    public FastJsonHttpMessageConverter build() {
        return build(APPLICATION_JSON);
    }

    public FastJsonHttpMessageConverter build(MediaType... mediaTypes) {
        val fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(newArrayList(mediaTypes));
        return fastJsonHttpMessageConverter;
    }
}
