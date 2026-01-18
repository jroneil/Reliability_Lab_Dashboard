# Reliability Lab Dashboard

A Spring Boot 3.x demonstration environment for observing service behavior under load/failure and validating resilience patterns.

## What it Demonstrates
- **Naive behavior**: How slow or failing dependencies can exhaust threads and increase error rates.
- **Timeouts**: How setting strict bounds (200ms) prevents slow dependencies from hanging requests indefinitely.
- **Resilience4j Patterns**: How Circuit Breakers and Bulkheads protect the system and provide fast feedback.

## Tech Stack
- **Backend**: Spring Boot 3.2, Java 17, Resilience4j, Maven
- **Frontend**: JSP, Chart.js, Vanilla CSS

## Getting Started
1. Clone the repo
2. Run the application:
   ```bash
   mvn spring-boot:run
   ```
3. Open your browser to: [http://localhost:8080/](http://localhost:8080/)

## Scenarios
- **Slow Dependency**: Mid-run, the dependency latency spikes by 250ms. Observe how p95/p99 latency increases.
- **Failing Dependency**: Mid-run, the dependency failure rate jumps to 30%. Watch the Error Rate metric and chart.
- **Traffic Spike**: Mid-run, the concurrency level doubles. Observe throughput and queuing effects.

## Resilience Modes
- **Naive**: Direct call. Dangerous for slow scenarios.
- **Timeouts**: Enforces a 200ms limit. Turn latency spikes into fast errors.
- **Full Resilience**: Adds a Circuit Breaker. After a threshold of failures, it stops calling the dependency entirely (Open State) to allow it to recover.

## What "Good" Looks Like
- **Latency**: p95 stays below 250ms even during disruptions.
- **Availability**: When disruption occurs, the system should either maintain success or fail fast (Circuit Breaker) rather than hanging.
- **Recovery**: The metrics should stabilize quickly after the disruption window ends.
