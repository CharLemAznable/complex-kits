package com.github.charlemaznable.core.spring;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequestWrapper;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.github.charlemaznable.core.net.Http.dealRequestBodyStream;
import static com.github.charlemaznable.core.net.Http.fetchParameterMap;
import static com.github.charlemaznable.core.spring.MutableHttpServletElf.mutableRequest;
import static com.github.charlemaznable.core.spring.MutableHttpServletElf.setRequestBody;
import static com.github.charlemaznable.core.spring.MutableHttpServletElf.setRequestParameter;
import static com.github.charlemaznable.core.spring.MutableHttpServletElf.setRequestParameterMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        val body12 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body12);
        val body13 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body1, body13);

        val body2 = "你好world";
        setRequestBody(mutableRequest, body2);
        val body21 = mutableRequest.getRequestBody();
        assertEquals(body2, body21);
        val body22 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body22);
        val body23 = dealRequestBodyStream(mutableRequest, "UTF-8");
        assertEquals(body2, body23);

        assertDoesNotThrow((Executable)
                () -> setRequestBody(null, body2));
    }

    @Test
    public void testParameter() {
        val key1 = "key1";
        val value1 = "value1";

        val mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(key1, value1);
        mockRequest.setParameter("empty");

        val mutableRequest = new MutableHttpServletRequest(mockRequest);
        assertNull(mutableRequest.getParameter("nonExists"));
        assertNull(mutableRequest.getParameter("empty"));

        val value11 = mutableRequest.getParameter(key1);
        assertEquals(value1, value11);
        val value12 = fetchParameterMap(mutableRequest).get(key1);
        assertEquals(value1, value12);

        val key2 = "key2";
        val value2 = "value2";
        setRequestParameter(mutableRequest, key2, value2);
        val value21 = mutableRequest.getParameter(key2);
        assertEquals(value2, value21);
        val value22 = fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value2, value22);

        assertDoesNotThrow((Executable)
                () -> setRequestParameter(null, key2, value2));

        val value3 = "value3";
        setRequestParameterMap(mutableRequest, of(key2, value3));
        val value31 = mutableRequest.getParameterMap().get(key2)[0];
        assertEquals(value3, value31);
        val value32 = fetchParameterMap(mutableRequest).get(key2);
        assertEquals(value3, value32);

        assertDoesNotThrow((Executable)
                () -> setRequestParameterMap(null, of(key2, value3)));

        assertDoesNotThrow((Executable)
                () -> mutableRequest.setParameter("SET_KEY", null));
        assertNull(mutableRequest.getParameterValues("SET_KEY"));
        assertDoesNotThrow((Executable)
                () -> mutableRequest.setParameter("SET_KEY", "SET_VALUE"));
        assertEquals("SET_VALUE", mutableRequest.getParameterValues("SET_KEY")[0]);
        assertDoesNotThrow((Executable)
                () -> mutableRequest.setParameter("SET_KEY", new String[]{"SET_VALUE1", "SET_VALUE2"}));
        assertEquals("SET_VALUE1", mutableRequest.getParameterValues("SET_KEY")[0]);
        assertEquals("SET_VALUE2", mutableRequest.getParameterValues("SET_KEY")[1]);
        assertDoesNotThrow((Executable)
                () -> mutableRequest.setParameter("SET_KEY", 123));
        assertEquals("123", mutableRequest.getParameterValues("SET_KEY")[0]);
    }

    @Test
    public void testWrapper() {
        val mockRequest = new MockHttpServletRequest();
        val mockWrapper = new HttpServletRequestWrapper(mockRequest);
        assertNull(mutableRequest(mockWrapper));

        val mutableRequest = new MutableHttpServletRequest(mockRequest);
        val mutableWrapper = new HttpServletRequestWrapper(mutableRequest);
        assertNotNull(mutableRequest(mutableWrapper));
    }
}
