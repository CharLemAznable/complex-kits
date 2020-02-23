package com.github.charlemaznable.core.miner.testClass;

import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.checkNull;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestMinerDataIdProvider implements DataIdProvider {

    private final TestMinerDataId testMinerDataId;

    @Autowired(required = false)
    public TestMinerDataIdProvider() {
        this(null);
    }

    @Inject
    @Autowired(required = false)
    public TestMinerDataIdProvider(@Nullable TestMinerDataId testMinerDataId) {
        this.testMinerDataId = testMinerDataId;
    }

    @Override
    public String dataId(Class<?> minerClass, Method method) {
        return checkNull(this.testMinerDataId, () -> "error", TestMinerDataId::getDataId);
    }
}
