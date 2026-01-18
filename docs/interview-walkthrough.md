# Interview Walkthrough: Reliability Lab

This document serves as a guide for presenting this project during a technical interview. It focuses on the engineering tradeoffs and system design insights rather than just code.

---

## 1. The Hook (The "Why")
"I built the Reliability Lab to solve a common problem in distributed systems: **latent failures.** Most developers understand hard failures (500s), but many struggle to predict how a dependency slowing down can bring an entire system to its knees via thread exhaustion. This lab makes those invisible failures visible through live metrics and charts."

## 2. The Demonstration (The "What")
*Recommended flow:*
1. **The Naive Disaster**: Run "Slow Dependency" in "Naive" mode. Point to the chart and say: "Notice how p95 latency matches the dependency slowdown. In a real system, your request threads would be tied up here, eventually starving your other APIs."
2. **The Fast Failure**: Run it again in "Timeout" mode. "Here, we've traded a hang for an error. The P95 stays flat. The system stays responsive, even if the feature using this dependency is down."
3. **The Protection**: Run in "Circuit Breaker" mode. "Once we hit a failure threshold, the circuit opens. We stop even trying to call the broken service, giving it room to recover and providing instantaneous feedback to the user."

## 3. High-Level Design (The "How")
*   **Decoupled Scenarios**: I separated the "disruption logic" (scenario) from the "resilience logic" (mode). This allows for 9 different permutations to test.
*   **In-Memory Metrics**: To keep the project lightweight and infrastructure-free, I used a high-performance in-memory bucket system (`TimeBucket.java`) to aggregate results in real-time.
*   **Explicit Wiring**: I intentionally wrote some of the resilience logic manually (using `CompletableFuture`) rather than relying solely on Resilience4j annotations to make the behavior more deterministic and visible for the demo.

## 4. Key Engineering Tradeoffs
*   **Availability vs. Latency**: "By using timeouts and circuit breakers, I'm choosing to lower availability (returning an error) in order to protect the system's overall latency and stability."
*   **Consistency vs. Speed**: "The chart data is eventually consistent; the UI polls the backend every second. This kept the frontend logic simple while providing a 'live' feel."

## 5. Potential Follow-up Questions
*   **Q: How would you scale this?**
    *   *A: Move metrics to Prometheus/Micrometer and storage to Redis/Elasticsearch if we needed to track thousands of concurrent simulations.*
*   **Q: Why Java 17/Spring Boot 3?**
    *   *A: To leverage Virtual Threads (if applicable) and the latest Resilience4j native Spring Boot support.*
