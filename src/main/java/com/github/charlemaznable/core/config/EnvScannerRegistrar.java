package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.EnvFactory.EnvLoader;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.core.config.EnvFactory.springEnvLoader;

public final class EnvScannerRegistrar extends SpringScannerRegistrar {

    private static EnvLoader springEnvLoader = springEnvLoader();

    public EnvScannerRegistrar() {
        super(EnvScan.class, EnvFactoryBean.class, EnvConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class EnvFactoryBean extends SpringFactoryBean {

        public EnvFactoryBean() {
            super(springEnvLoader::getEnv);
        }
    }
}
