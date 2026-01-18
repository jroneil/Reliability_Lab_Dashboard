package com.lab.reliability.web;

import com.lab.reliability.lab.ScenarioContext;
import com.lab.reliability.resilience.ResilienceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkController {

    private final ResilienceService resilienceService;

    public WorkController(ResilienceService resilienceService) {
        this.resilienceService = resilienceService;
    }

    @GetMapping("/api/work")
    public ResponseEntity<String> work(
            @RequestParam String scenario,
            @RequestParam String mode,
            @RequestParam int durationSec,
            @RequestParam long startTimeMs) {

        ScenarioContext ctx = new ScenarioContext(scenario, durationSec, startTimeMs);

        try {
            resilienceService.execute(mode, ctx);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
