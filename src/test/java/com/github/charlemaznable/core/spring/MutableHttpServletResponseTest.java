package com.github.charlemaznable.core.spring;

import com.google.common.base.Charsets;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponseWrapper;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.appendResponseContent;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.appendResponseContentByString;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.getResponseContent;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.getResponseContentAsString;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.mutableResponse;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.mutateResponse;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setResponseContent;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setResponseContentByString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MutableHttpServletResponseTest {

    @SneakyThrows
    @Test
    public void testContent() {
        val content = "Hello世界";

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val outputStream = mutableResponse.getOutputStream();
        outputStream.write(bytes(content));
        outputStream.flush();
        val content1 = mutableResponse.getContent();
        assertEquals(content, string(content1));

        setResponseContent(mutableResponse, bytes(content + content));
        val content2 = getResponseContent(mutableResponse);
        assertEquals(content + content, string(content2));

        appendResponseContent(mutableResponse, bytes(content));
        val content3 = getResponseContent(mutableResponse);
        assertEquals(content + content + content, string(content3));

        val mockContent = mockResponse.getContentAsByteArray();
        assertEquals(0, mockContent.length);
    }

    @SneakyThrows
    @Test
    public void testContentAsString() {
        val content = "Hello世界";

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val writer = mutableResponse.getWriter();
        writer.write(content);
        writer.flush();
        val content1 = mutableResponse.getContentAsString();
        assertEquals(content, content1);

        setResponseContentByString(mutableResponse, content + content);
        val content2 = getResponseContentAsString(mutableResponse);
        assertEquals(content + content, content2);

        appendResponseContentByString(mutableResponse, content);
        val content3 = getResponseContentAsString(mutableResponse);
        assertEquals(content + content + content, content3);

        val mockContent = mockResponse.getContentAsString();
        assertEquals("", mockContent);
    }

    @SneakyThrows
    @Test
    public void testContentAsStringWithCharset() {
        val content = string(bytes("Hello世界"), Charsets.ISO_8859_1);

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val outputStream = mutableResponse.getOutputStream();
        outputStream.write(bytes(content, Charsets.ISO_8859_1));
        outputStream.flush();
        val content1 = mutableResponse.getContentAsString(Charsets.ISO_8859_1);
        assertEquals(content, content1);

        setResponseContentByString(mutableResponse, content + content, Charsets.ISO_8859_1);
        val content2 = getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content, content2);

        appendResponseContentByString(mutableResponse, content, Charsets.ISO_8859_1);
        val content3 = getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content + content, content3);
    }

    @Test
    public void testWrapper() {
        val mockResponse = new MockHttpServletResponse();
        val mockWrapper = new HttpServletResponseWrapper(mockResponse);
        assertNull(mutableResponse(mockWrapper));

        mockWrapper.setStatus(404);
        mutateResponse(mockWrapper, response -> response.setStatus(500));
        assertEquals(404, mockWrapper.getStatus());

        val mutableResponse = new MutableHttpServletResponse(mockResponse);
        val mutableWrapper = new HttpServletResponseWrapper(mutableResponse);
        assertNotNull(mutableResponse(mutableWrapper));

        mutableWrapper.setStatus(404);
        mutateResponse(mutableWrapper, response -> {
            response.setStatus(500);
            response.setContentByString("mockWrapper");
        });
        assertEquals(500, mutableWrapper.getStatus());
        assertEquals("mockWrapper", getResponseContentAsString(mutableWrapper));
    }
}