package com.github.charlemaznable.core.miner.parser;

import com.github.charlemaznable.core.miner.MinerStoneParse.MinerStoneParser;
import com.github.charlemaznable.vertx.diamond.VertxDiamondElf;

public final class VertxOptionsParser implements MinerStoneParser {

    @Override
    public Object parse(String stone, Class<?> clazz) {
        return VertxDiamondElf.parseStoneToVertxOptions(stone);
    }
}
