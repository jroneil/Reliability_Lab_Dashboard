package com.lab.reliability.service;

import com.lab.reliability.lab.RunResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RunStore {
    private final Map<String, RunResult> runs = new ConcurrentHashMap<>();
    private String latestRunId;

    public void save(RunResult result) {
        runs.put(result.runId(), result);
        latestRunId = result.runId();
    }

    public Optional<RunResult> findById(String runId) {
        return Optional.ofNullable(runs.get(runId));
    }

    public Optional<RunResult> findLatest() {
        if (latestRunId == null)
            return Optional.empty();
        return findById(latestRunId);
    }
}
