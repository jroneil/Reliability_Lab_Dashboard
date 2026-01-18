# ADR 002: In-Memory Result Storage

## Status
Accepted

## Context
The Reliability Lab generates a high volume of metrics during a 30-60 second simulation run (e.g., latency for every individual call). These results are needed only for the duration of the visualization.

## Decision
We decided to store all simulation results in-memory using concurrent data structures, rather than using a database (SQL or NoSQL).

## Consequences
1. **Zero Infrastructure**: The project can be run with just `mvn spring-boot:run` without requiring a running database or container.
2. **Speed**: In-memory access provides the low latency needed to render live charts with thousands of data points without bottlenecks.
3. **Volatility**: Results are lost when the server restarts. This is an acceptable tradeoff for a lab/demo environment where data persistence is not a requirement.
