package com.github.charlemaznable.core.miner.testminer;

import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestMinerDataIdWrapper implements DataIdProvider {

    private final TestMinerDataId testMinerDataId;

    @Autowired(required = false)
    public TestMinerDataIdWrapper() {
        this(null);
    }

    @Inject
    @Autowired(required = false)
    public TestMinerDataIdWrapper(@Nullable TestMinerDataId testMinerDataId) {
        this.testMinerDataId = nullThen(testMinerDataId, () -> new TestMinerDataId() {
            @Override
            public String dataId(Class<?> minerClass, Method method) {
                return "error";
            }
        });
    }

    @Override
    public String dataId(Class<?> minerClass, Method method) {
        return testMinerDataId.dataId(minerClass, method);
    }
}
