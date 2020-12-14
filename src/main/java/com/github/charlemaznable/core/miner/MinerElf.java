package com.github.charlemaznable.core.miner;

import com.moandjiezana.toml.Toml;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.impl.PropertiesBasedMiner;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.n3r.diamond.client.impl.DiamondUtils.tryDecrypt;

@Slf4j
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

    public static Minerable parseStoneToMinerable(String stone) {
        return new PropertiesBasedMiner(parseStoneToProperties(stone));
    }

    public static Properties parseStoneToProperties(String stone) {
        Properties properties = new Properties();
        if (stone != null) {
            try {
                if (startsWithIgnoreCase(stone, "# toml")) {
                    properties.putAll(new Toml().read(stone).toMap());
                } else {
                    properties.load(new StringReader(stone));
                }
            } catch (Exception e) {
                log.warn("Parse stone to properties failed:", e);
            }
        }
        return tryDecrypt(properties);
    }
}
