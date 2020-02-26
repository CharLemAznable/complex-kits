package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.MinerScan;
import com.github.charlemaznable.core.miner.testminer.TestMinerDataId;
import com.github.charlemaznable.core.miner.testminer.TestMinerScanAnchor;
import com.github.charlemaznable.core.spring.ComplexImport;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.miner.MinerFactory.springMinerLoader;
import static org.joor.Reflect.on;

@Configuration
@ComplexImport
@MinerScan(basePackageClasses = TestMinerScanAnchor.class)
public class MinerSpringConfiguration {

    @Bean
    public TestMinerDataId testMinerDataId() {
        return new TestMinerDataId() {
            @Override
            public String dataId(Class<?> minerClass, Method method) {
                return "long";
            }
        };
    }

    @PostConstruct
    public void postConstruct() {
        on(springMinerLoader()).field("minerCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
