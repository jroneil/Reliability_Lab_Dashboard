-Design phase: Used Opus 4.5 to define system boundaries, scenarios, and metrics.

- Implementation phase: Used Gemini Fast for scaffolding controllers, models, and UI wiring.

- Human decisions:

  - Chose JSP over React for speed and clarity

  - Rejected over-engineering (no DB, no auth, no metrics backend)

  - Simplified resilience wiring to a single choke point

  - Validation: Manually verified metrics, sanity-checked percentiles, and reviewed failure modes.