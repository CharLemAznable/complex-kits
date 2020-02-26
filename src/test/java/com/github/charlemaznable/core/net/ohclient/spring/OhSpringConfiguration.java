package com.github.charlemaznable.core.net.ohclient.spring;

import com.github.charlemaznable.core.miner.MinerScan;
import com.github.charlemaznable.core.net.ohclient.OhScan;
import com.github.charlemaznable.core.net.ohclient.testclient.TestClientScanAnchor;
import com.github.charlemaznable.core.spring.ComplexComponentScan;
import com.github.charlemaznable.core.spring.ComplexImport;
import org.n3r.diamond.client.impl.MockDiamondServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.core.miner.MinerFactory.springMinerLoader;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.springOhLoader;
import static org.joor.Reflect.on;

@ComplexImport
@ComplexComponentScan(basePackageClasses = TestClientScanAnchor.class)
@MinerScan(basePackageClasses = TestClientScanAnchor.class)
@OhScan(basePackageClasses = TestClientScanAnchor.class)
public class OhSpringConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(springMinerLoader()).field("minerCache").call("invalidateAll");
        on(springOhLoader()).field("ohCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
