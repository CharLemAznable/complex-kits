package com.github.charlemaznable.core.miner.spring;

import com.github.charlemaznable.core.miner.MinerConfigException;
import com.github.charlemaznable.core.miner.testminer.TestMiner;
import com.github.charlemaznable.core.miner.testminer.TestMinerConcrete;
import com.github.charlemaznable.core.miner.testminer.TestMinerNone;
import com.github.charlemaznable.core.spring.SpringContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MinerSpringNakedConfiguration.class)
public class MinerSpringNakedTest {

    @Test
    public void testMinerNaked() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        var testMiner = getMiner(TestMiner.class);
        assertNotNull(testMiner);
        assertEquals("John", testMiner.name());
        assertEquals("John Doe", testMiner.full());
        assertThrows(NullPointerException.class, testMiner::longName);
        assertNull(testMiner.longWrap());
        assertEquals("xyz", testMiner.abc("xyz"));
        assertNull(testMiner.abc(null));

        assertThrows(MinerConfigException.class,
                () -> getMiner(TestMinerConcrete.class));

        assertThrows(MinerConfigException.class,
                () -> getMiner(TestMinerNone.class));

        ApplicationContext applicationContext = onClass(SpringContext.class)
                .field("applicationContext").get();
        assertThrows(NoSuchBeanDefinitionException.class, () ->
                applicationContext.getBean(TestMiner.class));
        assertNull(SpringContext.getBeanOrCreate(TestMiner.class));
    }
}
