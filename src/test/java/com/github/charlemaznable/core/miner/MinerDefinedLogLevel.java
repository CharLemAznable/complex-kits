package com.github.charlemaznable.core.miner;

import ch.qos.logback.core.PropertyDefinerBase;
import org.n3r.diamond.client.Minerable;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;

public class MinerDefinedLogLevel extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return getMiner(MinerDefiner.class).getString("complex-kits", "INFO");
    }

    @MinerConfig(group = "LogLevel", dataId = "default")
    public interface MinerDefiner extends Minerable {}
}
