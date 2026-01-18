package com.lab.reliability.service;

import com.lab.reliability.lab.RunRequest;
import com.lab.reliability.lab.RunResult;
import com.lab.reliability.lab.TimeBucket;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadRunner {
    private final RunStore runStore;
    private final RecommendationEngine recommendationEngine;
    private final RestTemplate restTemplate = new RestTemplate();

    public LoadRunner(RunStore runStore, RecommendationEngine recommendationEngine) {
        this.runStore = runStore;
        this.recommendationEngine = recommendationEngine;
    }

    @Async
    public void runSimulation(String runId, RunRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        List<TimeBucket> buckets = new ArrayList<>();
        long startTimeMs = System.currentTimeMillis();
        AtomicInteger activeThreads = new AtomicInteger(0);

        // Initialize first bucket
        RunResult.Status status = RunResult.Status.RUNNING;
        saveInitialResult(runId, startTime);

        ExecutorService executor = Executors.newFixedThreadPool(request.threads());
        int totalSeconds = request.durationSec();

        try {
            for (int i = 0; i < totalSeconds; i++) {
                TimeBucket bucket = new TimeBucket(i);
                buckets.add(bucket);

                // Concurrency increases mid-run if spike scenario
                int currentThreads = request.threads();
                if ("spike".equals(request.scenario()) && i >= totalSeconds / 3 && i <= 2 * totalSeconds / 3) {
                    currentThreads *= 2;
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int t = 0; t < currentThreads; t++) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        activeThreads.incrementAndGet();
                        long start = System.currentTimeMillis();
                        try {
                            String url = String.format(
                                    "http://localhost:8080/api/work?scenario=%s&mode=%s&durationSec=%d&startTimeMs=%d",
                                    request.scenario(), request.mode(), request.durationSec(), startTimeMs);
                            restTemplate.getForObject(url, String.class);
                            bucket.recordSuccess(System.currentTimeMillis() - start);
                        } catch (Exception e) {
                            bucket.recordError();
                        } finally {
                            activeThreads.decrementAndGet();
                        }
                    }, executor));
                }

                // Wait for the second to finish or tasks to finish
                Thread.sleep(1000);

                // Update progress in store
                updateProgress(runId, startTime, buckets, activeThreads.get());
            }

            status = RunResult.Status.DONE;
        } catch (Exception e) {
            status = RunResult.Status.FAILED;
        } finally {
            executor.shutdownNow();
            finalizeResult(runId, startTime, status, buckets, activeThreads.get(), request);
        }
    }

    private void saveInitialResult(String runId, LocalDateTime startTime) {
        RunResult result = new RunResult(runId, startTime, null, RunResult.Status.RUNNING,
                new RunResult.Summary(0, 0, 0, 0, 0), new ArrayList<>(), new ArrayList<>(),
                new RunResult.Peaks(0, 0), "Preparing simulation...", new ArrayList<>());
        runStore.save(result);
    }

    private void updateProgress(String runId, LocalDateTime startTime, List<TimeBucket> buckets, int peakThreads) {
        RunResult current = compileResult(runId, startTime, RunResult.Status.RUNNING, buckets, peakThreads, null);
        runStore.save(current);
    }

    private void finalizeResult(String runId, LocalDateTime startTime, RunResult.Status status,
            List<TimeBucket> buckets, int peakThreads, RunRequest request) {
        RunResult finalResult = compileResult(runId, startTime, status, buckets, peakThreads, request);
        runStore.save(finalResult);
    }

    private RunResult compileResult(String runId, LocalDateTime startTime, RunResult.Status status,
            List<TimeBucket> buckets, int peakThreads, RunRequest request) {
        List<RunResult.LatencyPoint> seriesLatency = new ArrayList<>();
        List<RunResult.ErrorPoint> seriesErrors = new ArrayList<>();

        long totalSuccess = 0;
        long totalErrors = 0;

        for (TimeBucket b : buckets) {
            seriesLatency.add(new RunResult.LatencyPoint(b.getTSec(),
                    b.getPercentile(50), b.getPercentile(95), b.getPercentile(99)));
            seriesErrors.add(new RunResult.ErrorPoint(b.getTSec(), (int) b.getErrors(), (int) b.getTotal()));

            totalSuccess += b.getSuccesses();
            totalErrors += b.getErrors();
            // In a real app we'd use a TDigest or similar, here we'll just approximate from
            // buckets if needed,
            // but let's just use the last bucket's distribution or simplified overall
        }

        double total = totalSuccess + totalErrors;
        double errorRate = total == 0 ? 0 : (totalErrors / total) * 100.0;
        double throughput = total / (buckets.isEmpty() ? 1 : buckets.size());

        // Simple overall percentile approximation (last bucket or avg)
        double p50 = buckets.isEmpty() ? 0 : buckets.get(buckets.size() - 1).getPercentile(50);
        double p95 = buckets.isEmpty() ? 0 : buckets.get(buckets.size() - 1).getPercentile(95);
        double p99 = buckets.isEmpty() ? 0 : buckets.get(buckets.size() - 1).getPercentile(99);

        RunResult.Summary summary = new RunResult.Summary(p50, p95, p99, errorRate, throughput);

        String rec = request != null ? recommendationEngine.getRecommendation(request, summary) : "Analyzing...";
        List<String> notes = request != null ? recommendationEngine.getRiskNotes(request, summary) : new ArrayList<>();

        return new RunResult(runId, startTime, status == RunResult.Status.RUNNING ? null : LocalDateTime.now(),
                status, summary, seriesLatency, seriesErrors, new RunResult.Peaks(peakThreads, 0), rec, notes);
    }
}
