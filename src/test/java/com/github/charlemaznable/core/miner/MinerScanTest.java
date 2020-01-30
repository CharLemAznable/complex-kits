package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.testClass.TestConfiguration;
import com.github.charlemaznable.core.miner.testClass.TestMiner;
import com.github.charlemaznable.core.miner.testClass.TestMiner2;
import com.github.charlemaznable.core.miner.testClass.TestSpringContext;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class MinerScanTest {

    @BeforeAll
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testMiner() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val minerDefault = TestSpringContext.getBean(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertEquals("John Doe Richard", minerDefault.longName());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        val minerDefault2 = TestSpringContext.getBean(TestMiner2.class);
        assertNull(minerDefault2);
    }
}
