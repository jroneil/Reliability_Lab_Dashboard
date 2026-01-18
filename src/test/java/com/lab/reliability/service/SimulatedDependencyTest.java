package com.lab.reliability.service;

import com.lab.reliability.lab.ScenarioContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulatedDependencyTest {

    private final SimulatedDependency dependency = new SimulatedDependency();

    @Test
    void testCallCompletesNormallyInNormalConditions() {
        ScenarioContext ctx = new ScenarioContext("none", 30, System.currentTimeMillis());
        assertDoesNotThrow(() -> dependency.call(ctx));
    }

    @Test
    void testSlowScenarioIncreasesLatency() {
        // We set duration to 10s and start time to 5s ago, so we are at 50% (right in
        // the middle of 30-70% window)
        long startTime = System.currentTimeMillis() - 5000;
        ScenarioContext ctx = new ScenarioContext("slow", 10, startTime);

        long start = System.currentTimeMillis();
        dependency.call(ctx);
        long duration = System.currentTimeMillis() - start;

        // Base is 20ms, slow adds 250ms, jitter is 0-10ms.
        // Should be at least 270ms.
        assertTrue(duration >= 250, "Latency should be increased in slow scenario. Got: " + duration);
    }
}
