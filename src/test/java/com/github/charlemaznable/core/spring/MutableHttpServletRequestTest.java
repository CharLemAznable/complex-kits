package com.github.charlemaznable.core.spring;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequestWrapper;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.net.Http.dealRequestBodyStream;
import static com.github.charlemaznable.core.net.Http.fetchParameterMap;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.mutableRequest;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setRequestBody;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setRequestParameter;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setRequestParameterMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MutableHttpServletRequestTest {

    @Test
    public void testRequestBody() {
        var body1 = "Hello世界";

        var mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(bytes(body1));

        var mutableRequest = new MutableHttpServletRequest(mockRequest);
        var body11 = mutableRequest.getRequestBody();
        assertEquals(body1, body11);
        var body12 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body12);
        var body13 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body13);

        var body2 = "你好world";
        setRequestBody(mutableRequest, body2);
        var body21 = mutableRequest.getRequestBody();
        assertEquals(body2, body21);
        var body22 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body22);
        var body23 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body23);

        assertDoesNotThrow(() -> setRequestBody(null, body2));
    }

    @Test
    public void testParameter() {
        var key1 = "key1";
        var value1 = "value1";

        var mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(key1, value1);
        mockRequest.setParameter("empty");

        var mutableRequest = new MutableHttpServletRequest(mockRequest);
        assertNull(mutableRequest.getParameter("nonExists"));
        assertNull(mutableRequest.getParameter("empty"));

        var value11 = mutableRequest.getParameter(key1);
        assertEquals(value1, value11);
        var value12 = fetchParameterMap(mutableRequest).get(key1);
        assertEquals(value1, value12);

        var key2 = "key2";
        var value2 = "value2";
        setRequestParameter(mutableRequest, key2, value2);
        var value21 = mutableRequest.getParameter(key2);
        assertEquals(value2, value21);
        var value22 = fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value2, value22);

        assertDoesNotThrow(() -> setRequestParameter(null, key2, value2));

        var value3 = "value3";
        setRequestParameterMap(mutableRequest, of(key2, value3));
        var value31 = mutableRequest.getParameterMap().get(key2)[0];
        assertEquals(value3, value31);
        var value32 = fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value3, value32);

        assertDoesNotThrow(() -> setRequestParameterMap(null, of(key2, value3)));

        assertDoesNotThrow(() -> mutableRequest.setParameter("SET_KEY", null));
        assertNull(mutableRequest.getParameterValues("SET_KEY"));
        assertDoesNotThrow(() -> mutableRequest.setParameter("SET_KEY", "SET_VALUE"));
        assertEquals("SET_VALUE", mutableRequest.getParameterValues("SET_KEY")[0]);
        assertDoesNotThrow(() -> mutableRequest.setParameter("SET_KEY", new String[]{"SET_VALUE1", "SET_VALUE2"}));
        assertEquals("SET_VALUE1", mutableRequest.getParameterValues("SET_KEY")[0]);
        assertEquals("SET_VALUE2", mutableRequest.getParameterValues("SET_KEY")[1]);
        assertDoesNotThrow(() -> mutableRequest.setParameter("SET_KEY", 123));
        assertEquals("123", mutableRequest.getParameterValues("SET_KEY")[0]);
    }

    @Test
    public void testWrapper() {
        var mockRequest = new MockHttpServletRequest();
        var mockWrapper = new HttpServletRequestWrapper(mockRequest);
        assertNull(mutableRequest(mockWrapper));

        var mutableRequest = new MutableHttpServletRequest(mockRequest);
        var mutableWrapper = new HttpServletRequestWrapper(mutableRequest);
        assertNotNull(mutableRequest(mutableWrapper));
    }
}
