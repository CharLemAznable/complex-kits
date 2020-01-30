package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhMapping;

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
@OhMapping("${root}:41102")
public @interface TestClientMapping {
}
