package com.lab.reliability.resilience;

import com.lab.reliability.lab.ScenarioContext;
import com.lab.reliability.service.SimulatedDependency;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ResilienceService {
    private final SimulatedDependency dependency;

    public ResilienceService(SimulatedDependency dependency) {
        this.dependency = dependency;
    }

    public void execute(String mode, ScenarioContext ctx) {
        switch (mode) {
            case "timeouts":
                executeWithTimeout(ctx);
                break;
            case "cb":
                executeWithResilience(ctx).join();
                break;
            case "naive":
            default:
                dependency.call(ctx);
                break;
        }
    }

    private void executeWithTimeout(ScenarioContext ctx) {
        try {
            CompletableFuture.runAsync(() -> dependency.call(ctx))
                    .get(200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException("Execution failed or timed out", e);
        }
    }

    @CircuitBreaker(name = "dependency")
    @Bulkhead(name = "dependency")
    @TimeLimiter(name = "dependency")
    public CompletableFuture<Void> executeWithResilience(ScenarioContext ctx) {
        return CompletableFuture.runAsync(() -> dependency.call(ctx));
    }
}
