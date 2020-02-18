package com.github.charlemaznable.core.miner;

import lombok.val;
import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.Miner;

import java.util.Map;

import static com.github.charlemaznable.core.lang.Mapp.newHashMap;

public final class MinerElf {

    private MinerElf() {
        throw new UnsupportedOperationException();
    }

    public static StringSubstitutor minerAsSubstitutor(String group, String dataId) {
        val minerProps = new Miner(group).getProperties(dataId);
        Map<String, String> minerPropsMap = newHashMap();
        val propNames = minerProps.propertyNames();
        while (propNames.hasMoreElements()) {
            val propName = (String) propNames.nextElement();
            val propValue = minerProps.getProperty(propName);
            minerPropsMap.put(propName, propValue);
        }
        return new StringSubstitutor(minerPropsMap);
    }
}
