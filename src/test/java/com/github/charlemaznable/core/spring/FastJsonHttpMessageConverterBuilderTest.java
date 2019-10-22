package com.github.charlemaznable.core.spring;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.charlemaznable.core.lang.Mapp;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson.parser.Feature.OrderedField;
import static com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FastJsonHttpMessageConverterBuilderTest {

    @Test
    public void testFastJsonHttpMessageConverterBuilder() {
        val parseProcess = new ParseProcess() {};
        val serializeFilter = new SerializeFilter() {};
        val converter = new FastJsonHttpMessageConverterBuilder()
                .charset(UTF_8)
                .serializeConfig(SerializeConfig.getGlobalInstance())
                .parserConfig(ParserConfig.getGlobalInstance())
                .parseProcess(parseProcess)
                .serializerFeatures(WriteClassName)
                .serializeFilters(serializeFilter)
                .features(OrderedField)
                .classSerializeFilters(Mapp.newHashMap())
                .dateFormat("YYYY-MM-DD hh:mm:ss")
                .writeContentLength(true)
                .build();
        assertEquals(UTF_8, converter.getFastJsonConfig().getCharset());
        assertEquals(SerializeConfig.getGlobalInstance(), converter.getFastJsonConfig().getSerializeConfig());
        assertEquals(ParserConfig.getGlobalInstance(), converter.getFastJsonConfig().getParserConfig());
        assertEquals(parseProcess, converter.getFastJsonConfig().getParseProcess());
        assertArrayEquals(new SerializerFeature[]{WriteClassName},
                converter.getFastJsonConfig().getSerializerFeatures());
        assertArrayEquals(new SerializeFilter[]{serializeFilter},
                converter.getFastJsonConfig().getSerializeFilters());
        assertArrayEquals(new Feature[]{OrderedField},
                converter.getFastJsonConfig().getFeatures());
        assertTrue(converter.getFastJsonConfig().getClassSerializeFilters().isEmpty());
        assertEquals("YYYY-MM-DD hh:mm:ss", converter.getFastJsonConfig().getDateFormat());
        assertTrue(converter.getFastJsonConfig().isWriteContentLength());
    }
}
