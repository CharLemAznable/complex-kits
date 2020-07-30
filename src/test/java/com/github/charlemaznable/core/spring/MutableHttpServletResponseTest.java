package com.github.charlemaznable.core.spring;

import com.google.common.base.Charsets;
import lombok.SneakyThrows;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MutableHttpServletResponseTest {

    @SneakyThrows
    @Test
    public void testContent() {
        var content = "Hello世界";

        var mockResponse = new MockHttpServletResponse();
        var mutableResponse = new MutableHttpServletResponse(mockResponse);

        var outputStream = mutableResponse.getOutputStream();
        outputStream.write(bytes(content));
        outputStream.flush();
        var content1 = mutableResponse.getContent();
        assertEquals(content, string(content1));

        setResponseContent(mutableResponse, bytes(content + content));
        var content2 = getResponseContent(mutableResponse);
        assertEquals(content + content, string(content2));

        assertDoesNotThrow(() -> setResponseContent(null, bytes(content + content)));

        appendResponseContent(mutableResponse, bytes(content));
        var content3 = getResponseContent(mutableResponse);
        assertEquals(content + content + content, string(content3));

        assertDoesNotThrow(() -> appendResponseContent(null, bytes(content)));

        var mockContent = mockResponse.getContentAsByteArray();
        assertEquals(0, mockContent.length);

        var emptyContent = getResponseContent(null);
        assertEquals(0, emptyContent.length);
    }

    @SneakyThrows
    @Test
    public void testContentAsString() {
        var content = "Hello世界";

        var mockResponse = new MockHttpServletResponse();
        var mutableResponse = new MutableHttpServletResponse(mockResponse);

        var writer1 = mutableResponse.getWriter();
        var writer = mutableResponse.getWriter();
        assertEquals(writer1, writer);
        writer.write(content);
        writer.flush();
        var content1 = mutableResponse.getContentAsString();
        assertEquals(content, content1);

        setResponseContentByString(mutableResponse, content + content);
        var content2 = getResponseContentAsString(mutableResponse);
        assertEquals(content + content, content2);

        assertDoesNotThrow(() -> setResponseContentByString(null, content + content));

        appendResponseContentByString(mutableResponse, content);
        var content3 = getResponseContentAsString(mutableResponse);
        assertEquals(content + content + content, content3);

        assertDoesNotThrow(() -> appendResponseContentByString(null, content));

        var mockContent = mockResponse.getContentAsString();
        assertEquals("", mockContent);

        assertDoesNotThrow(() -> getResponseContentAsString(null));
    }

    @SneakyThrows
    @Test
    public void testContentAsStringWithCharset() {
        var content = string(bytes("Hello世界"), Charsets.ISO_8859_1);

        var mockResponse = new MockHttpServletResponse();
        var mutableResponse = new MutableHttpServletResponse(mockResponse);

        var outputStream = mutableResponse.getOutputStream();
        byte[] bytes = bytes(content, Charsets.ISO_8859_1);
        assertNotNull(bytes);
        outputStream.write(bytes);
        outputStream.flush();
        var content1 = mutableResponse.getContentAsString(Charsets.ISO_8859_1);
        assertEquals(content, content1);

        setResponseContentByString(mutableResponse, content + content, Charsets.ISO_8859_1);
        var content2 = getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content, content2);

        assertDoesNotThrow(() -> setResponseContentByString(null, content + content, Charsets.ISO_8859_1));

        appendResponseContentByString(mutableResponse, content, Charsets.ISO_8859_1);
        var content3 = getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content + content, content3);

        assertDoesNotThrow(() -> appendResponseContentByString(null, content, Charsets.ISO_8859_1));

        assertDoesNotThrow(() -> getResponseContentAsString(null, Charsets.ISO_8859_1));
    }

    @Test
    public void testWrapper() {
        var mockResponse = new MockHttpServletResponse();
        var mockWrapper = new HttpServletResponseWrapper(mockResponse);
        assertNull(mutableResponse(mockWrapper));

        mockWrapper.setStatus(404);
        mutateResponse(mockWrapper, response -> response.setStatus(500));
        assertEquals(404, mockWrapper.getStatus());

        var mutableResponse = new MutableHttpServletResponse(mockResponse);
        var mutableWrapper = new HttpServletResponseWrapper(mutableResponse);
        assertNotNull(mutableResponse(mutableWrapper));

        mutableWrapper.setStatus(404);
        mutateResponse(mutableWrapper, response -> {
            response.setStatus(500);
            response.setContentByString("mockWrapper");
        });
        assertEquals(500, mutableWrapper.getStatus());
        assertEquals("mockWrapper", getResponseContentAsString(mutableWrapper));

        assertDoesNotThrow(() -> mutateResponse(mutableWrapper, null));
    }
}
