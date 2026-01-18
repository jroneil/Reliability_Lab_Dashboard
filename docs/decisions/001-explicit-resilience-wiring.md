# ADR 001: Explicit Resilience Wiring for Visibility

## Status
Accepted

## Context
In professional Spring Boot applications, developers typically use Resilience4j annotations (e.g., `@CircuitBreaker`, `@TimeLimiter`) to apply resilience patterns. These annotations use Spring AOP to wrap methods with the necessary logic, keeping the business code clean.

## Decision
For this lab environment, we have intentionally avoided annotations in the primary "Naive" vs "Timeout" logic, using explicit programming wiring instead.

## Consequences
1. **Visibility**: Explicit logic makes it easier for students and reviewers to see exactly how and where a timeout is applied (e.g., `CompletableFuture.get(limit, unit)`).
2. **Determinism**: By avoiding AOP for the base comparison, we reduce the "magic" and make the performance overhead of the patterns themselves more observable.
3. **Hybrid Approach**: We still include an `@CircuitBreaker` example in `ResilienceService` to demonstrate how to transition from the "lab" understanding to a production-ready implementation.
