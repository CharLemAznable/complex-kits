package com.github.charlemaznable.core.net.ohclient.internal;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import okhttp3.ConnectionPool;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static java.util.concurrent.Executors.newCachedThreadPool;

@NoArgsConstructor
@EqualsAndHashCode
public class OhDummy {

    static final Logger log = LoggerFactory.getLogger("OhClient");
    static final StringSubstitutor ohMinerSubstitutor;
    static final StringSubstitutor ohClassPathSubstitutor;
    static final ExecutorService ohExecutorService;
    static final ConnectionPool ohConnectionPool;

    static {
        ohMinerSubstitutor = minerAsSubstitutor("Env", "ohclient");
        ohClassPathSubstitutor = classResourceAsSubstitutor("ohclient.env.props");
        ohExecutorService = newCachedThreadPool();
        ohConnectionPool = new ConnectionPool();
    }

    static String substitute(String source) {
        return ohClassPathSubstitutor.replace(ohMinerSubstitutor.replace(source));
    }

    @Override
    public String toString() {
        return "OhClient@" + Integer.toHexString(hashCode());
    }
}
