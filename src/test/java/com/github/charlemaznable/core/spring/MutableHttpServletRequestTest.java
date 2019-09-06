package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.net.Http;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequestWrapper;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MutableHttpServletRequestTest {

    @Test
    public void testRequestBody() {
        val body1 = "Hello世界";

        val mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(bytes(body1));

        val mutableRequest = new MutableHttpServletRequest(mockRequest);
        val body11 = mutableRequest.getRequestBody();
        assertEquals(body1, body11);
        val body12 = Http.dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body12);
        val body13 = Http.dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body13);

        val body2 = "你好world";
        MutableHttpServletUtils.setRequestBody(mutableRequest, body2);
        val body21 = mutableRequest.getRequestBody();
        assertEquals(body2, body21);
        val body22 = Http.dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body22);
        val body23 = Http.dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body23);
    }

    @Test
    public void testParameter() {
        val key1 = "key1";
        val value1 = "value1";

        val mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(key1, value1);

        val mutableRequest = new MutableHttpServletRequest(mockRequest);
        val value11 = mutableRequest.getParameter(key1);
        assertEquals(value1, value11);
        val value12 = Http.fetchParameterMap(mutableRequest).get(key1);
        assertEquals(value1, value12);

        val key2 = "key2";
        val value2 = "value2";
        MutableHttpServletUtils.setRequestParameter(mutableRequest, key2, value2);
        val value21 = mutableRequest.getParameter(key2);
        assertEquals(value2, value21);
        val value22 = Http.fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value2, value22);

        val value3 = "value3";
        MutableHttpServletUtils.setRequestParameterMap(mutableRequest, of(key2, value3));
        val value31 = mutableRequest.getParameterMap().get(key2)[0];
        assertEquals(value3, value31);
        val value32 = Http.fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value3, value32);
    }

    @Test
    public void testWrapper() {
        val mockRequest = new MockHttpServletRequest();
        val mockWrapper = new HttpServletRequestWrapper(mockRequest);
        assertNull(MutableHttpServletUtils.mutableRequest(mockWrapper));

        val mutableRequest = new MutableHttpServletRequest(mockRequest);
        val mutableWrapper = new HttpServletRequestWrapper(mutableRequest);
        assertNotNull(MutableHttpServletUtils.mutableRequest(mutableWrapper));
    }
}
