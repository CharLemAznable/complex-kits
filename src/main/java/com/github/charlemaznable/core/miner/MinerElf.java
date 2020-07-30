package com.github.charlemaznable.core.miner;

import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.Miner;

import java.util.Map;

import static com.github.charlemaznable.core.lang.Mapp.newHashMap;

public final class MinerElf {

    private MinerElf() {
        throw new UnsupportedOperationException();
    }

    public static StringSubstitutor minerAsSubstitutor(String group, String dataId) {
        var minerProps = new Miner(group).getProperties(dataId);
        Map<String, String> minerPropsMap = newHashMap();
        var propNames = minerProps.propertyNames();
        while (propNames.hasMoreElements()) {
            var propName = (String) propNames.nextElement();
            var propValue = minerProps.getProperty(propName);
            minerPropsMap.put(propName, propValue);
        }
        return new StringSubstitutor(minerPropsMap);
    }
}
