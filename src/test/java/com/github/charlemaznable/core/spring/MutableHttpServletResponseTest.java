package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.MutableHttpServletResponse;
import com.github.charlemaznable.core.spring.MutableHttpServletUtils;
import com.google.common.base.Charsets;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponseWrapper;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MutableHttpServletResponseTest {

    @SneakyThrows
    @Test
    public void testContent() {
        String content = "Hello世界";

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val outputStream = mutableResponse.getOutputStream();
        outputStream.write(bytes(content));
        outputStream.flush();
        byte[] content1 = mutableResponse.getContent();
        assertEquals(content, string(content1));

        MutableHttpServletUtils.setResponseContent(mutableResponse, bytes(content + content));
        byte[] content2 = MutableHttpServletUtils.getResponseContent(mutableResponse);
        assertEquals(content + content, string(content2));

        MutableHttpServletUtils.appendResponseContent(mutableResponse, bytes(content));
        byte[] content3 = MutableHttpServletUtils.getResponseContent(mutableResponse);
        assertEquals(content + content + content, string(content3));

        byte[] mockContent = mockResponse.getContentAsByteArray();
        assertEquals(0, mockContent.length);
    }

    @SneakyThrows
    @Test
    public void testContentAsString() {
        String content = "Hello世界";

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val writer = mutableResponse.getWriter();
        writer.write(content);
        writer.flush();
        String content1 = mutableResponse.getContentAsString();
        assertEquals(content, content1);

        MutableHttpServletUtils.setResponseContentByString(mutableResponse, content + content);
        String content2 = MutableHttpServletUtils.getResponseContentAsString(mutableResponse);
        assertEquals(content + content, content2);

        MutableHttpServletUtils.appendResponseContentByString(mutableResponse, content);
        String content3 = MutableHttpServletUtils.getResponseContentAsString(mutableResponse);
        assertEquals(content + content + content, content3);

        String mockContent = mockResponse.getContentAsString();
        assertEquals("", mockContent);
    }

    @SneakyThrows
    @Test
    public void testContentAsStringWithCharset() {
        String content = string(bytes("Hello世界"), Charsets.ISO_8859_1);

        val mockResponse = new MockHttpServletResponse();
        val mutableResponse = new MutableHttpServletResponse(mockResponse);

        val outputStream = mutableResponse.getOutputStream();
        outputStream.write(bytes(content, Charsets.ISO_8859_1));
        outputStream.flush();
        String content1 = mutableResponse.getContentAsString(Charsets.ISO_8859_1);
        assertEquals(content, content1);

        MutableHttpServletUtils.setResponseContentByString(mutableResponse, content + content, Charsets.ISO_8859_1);
        String content2 = MutableHttpServletUtils.getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content, content2);

        MutableHttpServletUtils.appendResponseContentByString(mutableResponse, content, Charsets.ISO_8859_1);
        String content3 = MutableHttpServletUtils.getResponseContentAsString(mutableResponse, Charsets.ISO_8859_1);
        assertEquals(content + content + content, content3);
    }

    @Test
    public void testWrapper() {
        val mockResponse = new MockHttpServletResponse();
        val mockWrapper = new HttpServletResponseWrapper(mockResponse);
        assertNull(MutableHttpServletUtils.mutableResponse(mockWrapper));

        mockWrapper.setStatus(404);
        MutableHttpServletUtils.mutateResponse(mockWrapper, response -> response.setStatus(500));
        assertEquals(404, mockWrapper.getStatus());

        val mutableResponse = new MutableHttpServletResponse(mockResponse);
        val mutableWrapper = new HttpServletResponseWrapper(mutableResponse);
        assertNotNull(MutableHttpServletUtils.mutableResponse(mutableWrapper));

        mutableWrapper.setStatus(404);
        MutableHttpServletUtils.mutateResponse(mutableWrapper, response -> {
            response.setStatus(500);
            response.setContentByString("mockWrapper");
        });
        assertEquals(500, mutableWrapper.getStatus());
        assertEquals("mockWrapper", MutableHttpServletUtils.getResponseContentAsString(mutableWrapper));
    }
}
