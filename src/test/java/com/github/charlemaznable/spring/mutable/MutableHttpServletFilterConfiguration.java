package com.github.charlemaznable.spring.mutable;

import com.github.charlemaznable.spring.ComplexComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan
@Import(ComplexComponentScan.class)
public class MutableHttpServletFilterConfiguration {
}
