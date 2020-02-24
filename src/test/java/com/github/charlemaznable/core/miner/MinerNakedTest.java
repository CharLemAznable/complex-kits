package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.testClass.TestMiner;
import com.github.charlemaznable.core.miner.testClass.TestMiner2;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.beans.factory.BeanCreationException;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinerNakedTest {

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

        val minerDefault = getMiner(TestMiner.class);
        assertNotNull(minerDefault);
        assertEquals("John", minerDefault.name());
        assertEquals("John Doe", minerDefault.full());
        assertThrows(BeanCreationException.class, minerDefault::longName);
        assertNull(minerDefault.longWrap());
        assertEquals("xyz", minerDefault.abc("xyz"));
        assertNull(minerDefault.abc(null));

        assertThrows(MinerConfigException.class,
                () -> getMiner(TestMiner2.class));
    }
}
