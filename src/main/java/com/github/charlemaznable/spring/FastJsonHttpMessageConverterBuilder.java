package com.github.charlemaznable.spring;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.val;

import java.nio.charset.Charset;

import static com.github.charlemaznable.lang.Listt.newArrayList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

public class FastJsonHttpMessageConverterBuilder {

    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public FastJsonHttpMessageConverterBuilder charset(Charset charset) {
        this.fastJsonConfig.setCharset(charset);
        return this;
    }

    public FastJsonHttpMessageConverterBuilder dateFormat(String dateFormat) {
        this.fastJsonConfig.setDateFormat(dateFormat);
        return this;
    }

    public FastJsonHttpMessageConverter build() {
        val fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(newArrayList(APPLICATION_JSON_UTF8));
        return fastJsonHttpMessageConverter;
    }
}
