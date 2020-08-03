package com.github.charlemaznable.core.net;

import com.github.charlemaznable.core.net.httptest.HttpTestConfiguration;
import com.github.charlemaznable.core.net.httptest.HttpTestController;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HttpTestConfiguration.class)
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class HttpTest {

    private static MockMvc mockMvc;
    @Autowired
    private HttpTestController httpTestController;

    @BeforeAll
    public void beforeAll() {
        mockMvc = MockMvcBuilders.standaloneSetup(httpTestController).build();
    }

    @SneakyThrows
    @Test
    public void testResponse() {
        val responseJson = mockMvc.perform(get("/json"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("json", responseJson.getContentAsString());
        assertEquals("application/json; charset=UTF-8", responseJson.getContentType());

        val responseText = mockMvc.perform(get("/text"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("text", responseText.getContentAsString());
        assertEquals("text/plain; charset=UTF-8", responseText.getContentType());

        val responseHtml = mockMvc.perform(get("/html"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("html", responseHtml.getContentAsString());
        assertEquals("text/html; charset=UTF-8", responseHtml.getContentType());
    }

    @SneakyThrows
    @Test
    public void testResponseError() {
        val responseJson = mockMvc.perform(get("/json-error"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals("json", responseJson.getContentAsString());
        assertEquals("application/json; charset=UTF-8", responseJson.getContentType());

        val responseText = mockMvc.perform(get("/text-error"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals("text", responseText.getContentAsString());
        assertEquals("text/plain; charset=UTF-8", responseText.getContentType());

        val responseHtml = mockMvc.perform(get("/html-error"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals("html", responseHtml.getContentAsString());
        assertEquals("text/html; charset=UTF-8", responseHtml.getContentType());

        val responseHttpStatus = mockMvc.perform(get("/http-status-error"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), responseHttpStatus.getContentAsString());
        assertEquals("text/plain; charset=UTF-8", responseHttpStatus.getContentType());
    }

    @SneakyThrows
    @Test
    public void testParameter() {
        val response = mockMvc.perform(get("/parameter")
                .param("AAA", "aaa")
                .param("BBB", "bbb"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());

        val response2 = mockMvc.perform(get("/deal-parameter")
                .param("AAA", "aaa")
                .param("BBB", "bbb", "bbb"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response2.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testPathVariable() {
        val response = mockMvc.perform(get("/path-variable/aaa/bbb"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testHeader() {
        val response = mockMvc.perform(get("/header")
                .header("AAA", "aaa")
                .header("BBB", "bbb"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testCookie() {
        val response = mockMvc.perform(get("/cookie")
                .cookie(new Cookie("AAA", "aaa"))
                .cookie(new Cookie("BBB", "bbb")))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testRemoteAddr() {
        MockHttpServletResponse response = mockMvc.perform(get("/remote-addr")
                .header("x-forwarded-for", ",unknown,test.addr"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());

        response = mockMvc.perform(get("/remote-addr")
                .header("Proxy-Client-IP", "test.addr"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());

        response = mockMvc.perform(get("/remote-addr")
                .header("Proxy-Client-IP", "")
                .header("WL-Proxy-Client-IP", "test.addr"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());

        response = mockMvc.perform(get("/remote-addr")
                .header("Proxy-Client-IP", "unknown")
                .header("WL-Proxy-Client-IP", ""))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("127.0.0.1", response.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testBody() {
        byte[] content = bytes("The quick brown fox jumps over the lazy dog.");
        val response = mockMvc.perform(post("/body")
                .content(content))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());
    }

    @SneakyThrows
    @Test
    public void testAjax() {
        val response = mockMvc.perform(post("/ajax")
                .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("OK", response.getContentAsString());
    }
}
