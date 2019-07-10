package com.github.charlemaznable.miner;

import com.github.charlemaznable.spring.SpringFactoryBean;
import com.github.charlemaznable.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

public class MinerScannerRegistrar extends SpringScannerRegistrar {

    public MinerScannerRegistrar() {
        super(MinerScan.class, MinerFactoryBean.class, MinerConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return (classMetadata.isInterface() && classMetadata.isIndependent());
    }

    public static class MinerFactoryBean extends SpringFactoryBean {

        public MinerFactoryBean() {
            super(MinerFactory::getMiner);
        }
    }
}
