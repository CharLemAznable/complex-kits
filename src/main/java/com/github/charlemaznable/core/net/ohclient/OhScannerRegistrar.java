package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

public final class OhScannerRegistrar extends SpringScannerRegistrar {

    public OhScannerRegistrar() {
        super(OhScan.class, OhClientFactoryBean.class, OhClient.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class OhClientFactoryBean extends SpringFactoryBean {

        public OhClientFactoryBean() {
            super(OhFactory::getClient);
        }
    }
}
