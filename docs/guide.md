# Reliability Lab: Usage and Integration Guide

This guide explains how to use the Reliability Lab Dashboard to observe system behavior and how to implement these patterns in your own production systems.

---

## 1. How to Use the Dashboard

The dashboard is a sandbox to see how different "Resilience Modes" protect your application against "Disruption Scenarios."

### Step-by-Step Experiment
1.  **Set the Baseline**:
    - Select **Scenario**: `Slow Dependency`
    - Select **Mode**: `Naive`
    - Set **Duration**: `30s`
    - Click **Run Simulation**.
    - *Observation*: Note how the P95 latency spikes massively during the disruption window (the middle 10 seconds). The system is "hanging."

2.  **Apply Timeouts**:
    - Keep **Scenario**: `Slow Dependency`
    - Select **Mode**: `Timeouts`
    - Click **Run Simulation**.
    - *Observation*: The P95 latency stays flat at ~200ms. However, the **Error Rate** spikes. You have traded a "slow hang" for a "fast failure." This is usually better for system stability.

3.  **Apply Full Resilience**:
    - Select **Scenario**: `Failing Dependency`
    - Select **Mode**: `Circuit Breaker`
    - Click **Run Simulation**.
    - *Observation*: Initially, errors occur. Once the failure threshold is hit, the **Circuit Breaker opens**. In the charts, you'll see throughput drop for the dependency call, but the system stops wasting resources on a known-failing downstream service.

---

## 2. Implementing in an Existing System

To implement these patterns in a real Spring Boot application, follows these steps:

### A. Add Dependencies
Add the Resilience4j Spring Boot starter to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### B. Configure Patterns
Define your resilience thresholds in `application.yml`. Avoid hardcoding these values in code.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalService:
        registerHealthMap: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
  timelimiter:
    instances:
      externalService:
        timeoutDuration: 2s
  bulkhead:
    instances:
      externalService:
        maxConcurrentCalls: 10
        maxWaitDuration: 0
```

### C. Annotate Your Services
Use the annotations on your service methods that perform external calls (HTTP, DB, etc.).

```java
@Service
public class MyIntegrationService {

    @CircuitBreaker(name = "externalService", fallbackMethod = "fallback")
    @Bulkhead(name = "externalService")
    @TimeLimiter(name = "externalService")
    public CompletableFuture<String> callRemoteApi() {
        return CompletableFuture.supplyAsync(() -> {
            // Your actual remote call logic here
            return restTemplate.getForObject("...", String.class);
        });
    }

    // This method runs if the CB is open or the call fails/times out
    public CompletableFuture<String> fallback(Throwable t) {
        return CompletableFuture.completedFuture("Cached/Default Data");
    }
}
```

### D. Crucial Implementation Tips
1.  **Timeouts First**: Always set a timeout. If you use `TimeLimiter` with Resilience4j, your method **must** return a `CompletableFuture` or `Publisher`.
2.  **Choose the Right Window**: For high-traffic services, use a larger `slidingWindowSize` (e.g., 100). For low-traffic, use a smaller one (e.g., 10).
3.  **Monitor Your State**: Use the `resilience4j-micrometer` module to export dashboard metrics to Prometheus/Grafana so you can see when breakers open in production.
4.  **Bulkheads for Isolation**: Use Bulkheads to ensure that a failure in one "expensive" downstream service doesn't consume all the threads in your application, starving "cheap" services.
