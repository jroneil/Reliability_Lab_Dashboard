package com.lab.reliability.service;

import com.lab.reliability.lab.RunRequest;
import com.lab.reliability.lab.RunResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationEngine {

    public String getRecommendation(RunRequest req, RunResult.Summary summary) {
        if (summary.errorRatePct() > 10) {
            if ("naive".equals(req.mode())) {
                return "The system is failing under stress. Implementing a Circuit Breaker would prevent cascading failures and provide faster feedback.";
            } else if ("timeouts".equals(req.mode())) {
                return "Timeouts are catching slow calls, but the service is still attempting every request. A Circuit Breaker would help 'fail fast' and protect the downstream.";
            } else {
                return "The Circuit Breaker is active, protecting the system from high failure rates. Consider increasing capacity or tuning thresholds.";
            }
        }

        if (summary.p95Ms() > 200) {
            return "Latency is high. Consider implementing strict timeouts and a Bulkhead to prevent slow calls from exhausting all worker threads.";
        }

        return "Performance is within healthy bounds. Monitor for changes as load increases.";
    }

    public List<String> getRiskNotes(RunRequest req, RunResult.Summary summary) {
        List<String> notes = new ArrayList<>();

        if ("naive".equals(req.mode()) && "slow".equals(req.scenario())) {
            notes.add(
                    "Risk: Without timeouts, slow dependencies can cause thread exhaustion and take down the entire service.");
        }

        if (summary.errorRatePct() > 20) {
            notes.add("Critical: High error rate detected. Users are experiencing significant disruptions.");
        }

        if (req.threads() > 16 && summary.p99Ms() > 500) {
            notes.add(
                    "Note: High concurrency is causing queuing delays. Scaling horizontal instances might be necessary.");
        }

        if (notes.isEmpty()) {
            notes.add("No critical risks identified at current load levels.");
        }

        return notes;
    }
}
