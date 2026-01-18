package com.lab.reliability.web;

import com.lab.reliability.lab.RunRequest;
import com.lab.reliability.lab.RunResult;
import com.lab.reliability.service.LoadRunner;
import com.lab.reliability.service.RunStore;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/run")
public class RunController {

    private final LoadRunner loadRunner;
    private final RunStore runStore;

    public RunController(LoadRunner loadRunner, RunStore runStore) {
        this.loadRunner = loadRunner;
        this.runStore = runStore;
    }

    @PostMapping
    public Map<String, String> startRun(@RequestBody RunRequest request) {
        String runId = UUID.randomUUID().toString();
        loadRunner.runSimulation(runId, request);
        return Collections.singletonMap("runId", runId);
    }

    @GetMapping("/latest")
    public RunResult getLatest() {
        return runStore.findLatest().orElse(null);
    }

    @GetMapping("/{runId}")
    public RunResult getRun(@PathVariable String runId) {
        return runStore.findById(runId).orElse(null);
    }
}
