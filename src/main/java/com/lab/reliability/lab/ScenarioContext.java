package com.lab.reliability.lab;

public class ScenarioContext {
    private final String scenario;
    private final int durationSec;
    private final long startTimeMs;

    public ScenarioContext(String scenario, int durationSec, long startTimeMs) {
        this.scenario = scenario;
        this.durationSec = durationSec;
        this.startTimeMs = startTimeMs;
    }

    public boolean isInDisruptionWindow() {
        long elapsedSec = (System.currentTimeMillis() - startTimeMs) / 1000;
        double progress = (double) elapsedSec / durationSec;
        return progress >= 0.3 && progress <= 0.7;
    }

    public String getScenario() {
        return scenario;
    }

    public long getElapsedSec() {
        return (System.currentTimeMillis() - startTimeMs) / 1000;
    }
}
