package com.lab.reliability.service;

import com.lab.reliability.lab.ScenarioContext;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SimulatedDependency {
    private final Random random = new Random();

    public void call(ScenarioContext ctx) {
        String scenario = ctx.getScenario();
        boolean disrupted = ctx.isInDisruptionWindow();

        // Failure logic
        double failRate = 0.01; // 1% base
        if ("fail".equals(scenario) && disrupted) {
            failRate = 0.30; // 30% during disruption
        }

        if (random.nextDouble() < failRate) {
            throw new RuntimeException("Simulated dependency failure");
        }

        // Latency logic
        long baseLatency = 20;
        if ("slow".equals(scenario) && disrupted) {
            baseLatency += 250;
        }

        // Add some jitter
        long jitter = random.nextLong(10);

        try {
            Thread.sleep(baseLatency + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
