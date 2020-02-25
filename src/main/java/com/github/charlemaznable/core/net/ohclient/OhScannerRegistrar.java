package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.springOhLoader;

public final class OhScannerRegistrar extends SpringScannerRegistrar {

    private static OhLoader springOhLoader = springOhLoader();

    public OhScannerRegistrar() {
        super(OhScan.class, OhClientFactoryBean.class, OhClient.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class OhClientFactoryBean extends SpringFactoryBean {

        public OhClientFactoryBean() {
            super(springOhLoader::getClient);
        }
    }
}
