package com.github.charlemaznable.spring;

import com.github.charlemaznable.spring.testClass.MutableHttpServletFilterConfiguration;
import com.github.charlemaznable.spring.testClass.MutableHttpServletFilterController;
import com.github.charlemaznable.spring.testClass.MutableHttpServletFilterInterceptor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static com.github.charlemaznable.codec.Json.unJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MutableHttpServletFilterConfiguration.class)
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class MutableHttpServletFilterTest {

    @Autowired
    private MutableHttpServletFilterController mutableHttpServletFilterController;
    @Autowired
    private MutableHttpServletFilterInterceptor mutableHttpServletFilterInterceptor;
    @Autowired
    private MutableHttpServletFilter mutableHttpServletFilter;

    private static MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mutableHttpServletFilterController)
                .addMappedInterceptors(new String[]{"/**"}, mutableHttpServletFilterInterceptor)
                .addFilters(mutableHttpServletFilter).build();
    }

    @SneakyThrows
    @Test
    public void testSample() {
        val response = mockMvc.perform(get("/mutable-filter")
                .param("IN_REQUEST", "TRUE")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String responseContent = response.getContentAsString();
        Map<String, Object> responseMap = unJson(responseContent);
        assertEquals("TRUE", responseMap.get("IN_REQUEST"));
        assertEquals("TRUE", responseMap.get("IN_PREHANDLE"));
        assertEquals("TRUE", responseMap.get("IN_CONTROLLER"));
        assertEquals("TRUE", responseMap.get("IN_POSTHANDLE"));
    }
}
