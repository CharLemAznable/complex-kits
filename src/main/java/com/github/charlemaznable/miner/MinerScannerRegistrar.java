package com.github.charlemaznable.miner;

import com.github.charlemaznable.spring.SpringFactoryBean;
import com.github.charlemaznable.spring.SpringScannerRegistrar;

public class MinerScannerRegistrar extends SpringScannerRegistrar {

    public MinerScannerRegistrar() {
        super(MinerScan.class, MinerFactoryBean.class, MinerConfig.class);
    }

    public static class MinerFactoryBean extends SpringFactoryBean {

        public MinerFactoryBean() {
            super(MinerFactory::getMiner);
        }
    }
}
