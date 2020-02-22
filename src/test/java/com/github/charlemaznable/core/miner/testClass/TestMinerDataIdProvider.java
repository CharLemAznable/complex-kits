package com.github.charlemaznable.core.miner.testClass;

import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.checkNull;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestMinerDataIdProvider implements DataIdProvider {

    private final TestMinerDataId testMinerDataId;

    @Autowired(required = false)
    public TestMinerDataIdProvider() {
        this.testMinerDataId = null;
    }

    @Autowired(required = false)
    public TestMinerDataIdProvider(TestMinerDataId testMinerDataId) {
        this.testMinerDataId = testMinerDataId;
    }

    @Override
    public String dataId(Class<?> minerClass, Method method) {
        return checkNull(this.testMinerDataId, () -> "error", TestMinerDataId::getDataId);
    }
}
