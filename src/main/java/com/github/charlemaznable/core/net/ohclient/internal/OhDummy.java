package com.github.charlemaznable.core.net.ohclient.internal;

import lombok.NoArgsConstructor;
import okhttp3.ConnectionPool;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static java.util.Objects.isNull;
import static java.util.concurrent.Executors.newCachedThreadPool;

@NoArgsConstructor
public class OhDummy {

    static final Logger log = LoggerFactory.getLogger("OhClient");
    static StringSubstitutor ohMinerSubstitutor;
    static StringSubstitutor ohClassPathSubstitutor;
    static final ExecutorService ohExecutorService;
    static final ConnectionPool ohConnectionPool;

    static {
        ohExecutorService = newCachedThreadPool();
        ohConnectionPool = new ConnectionPool();
    }

    static String substitute(String source) {
        if (isNull(ohMinerSubstitutor)) {
            ohMinerSubstitutor = minerAsSubstitutor("Env", "ohclient");
        }
        if (isNull(ohClassPathSubstitutor)) {
            ohClassPathSubstitutor = classResourceAsSubstitutor("ohclient.env.props");
        }
        return ohClassPathSubstitutor.replace(ohMinerSubstitutor.replace(source));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OhDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "OhClient@" + Integer.toHexString(hashCode());
    }
}
