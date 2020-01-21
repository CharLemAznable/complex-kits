package com.github.charlemaznable.core.net.ohclient.internal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static java.util.concurrent.Executors.newCachedThreadPool;

@NoArgsConstructor
@EqualsAndHashCode
public class OhDummy {

    static final Logger log = LoggerFactory.getLogger("OhClient");
    static StringSubstitutor ohSubstitutor;
    static ExecutorService ohExecutorService;

    static {
        ohSubstitutor = classResourceAsSubstitutor("ohclient.env.props");
        ohExecutorService = newCachedThreadPool();
    }

    @Override
    public String toString() {
        return "OhClient@" + Integer.toHexString(hashCode());
    }
}
