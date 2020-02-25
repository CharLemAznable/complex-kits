package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.net.common.Mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Mapping("${root}:41102")
public @interface TestClientMapping {
}
