package com.lab.reliability.resilience;

import com.lab.reliability.lab.ScenarioContext;
import com.lab.reliability.service.SimulatedDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilienceServiceTest {

    @Mock
    private SimulatedDependency dependency;

    private ResilienceService resilienceService;

    @BeforeEach
    void setUp() {
        resilienceService = new ResilienceService(dependency);
    }

    @Test
    void testNaiveExecutionCallsDependency() {
        ScenarioContext ctx = new ScenarioContext("none", 30, System.currentTimeMillis());
        resilienceService.execute("naive", ctx);
        verify(dependency, times(1)).call(ctx);
    }

    @Test
    void testTimeoutExecutionCallsDependency() {
        ScenarioContext ctx = new ScenarioContext("none", 30, System.currentTimeMillis());
        resilienceService.execute("timeouts", ctx);
        verify(dependency, atLeastOnce()).call(any(ScenarioContext.class));
    }
}
