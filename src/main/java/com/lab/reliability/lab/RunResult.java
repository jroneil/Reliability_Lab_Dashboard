package com.lab.reliability.lab;

import java.time.LocalDateTime;
import java.util.List;

public record RunResult(
        String runId,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Status status,
        Summary summary,
        List<LatencyPoint> seriesLatency,
        List<ErrorPoint> seriesErrors,
        Peaks peaks,
        String recommendation,
        List<String> riskNotes) {
    public enum Status {
        RUNNING, DONE, FAILED
    }

    public record Summary(
            double p50Ms,
            double p95Ms,
            double p99Ms,
            double errorRatePct,
            double throughputRps) {
    }

    public record LatencyPoint(int tSec, double p50Ms, double p95Ms, double p99Ms) {
    }

    public record ErrorPoint(int tSec, int errors, int total) {
    }

    public record Peaks(int activeThreadsPeak, int queueDepthPeak) {
    }
}
