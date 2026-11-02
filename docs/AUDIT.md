# SF3JSwing Technical Audit
- Files larger than 1 MiB include a sample WAV file from the test module and several Xcode index artifacts within the game’s directory structure. These are tracked despite being unnecessary for Java compilation; their origin and utility should be verified—though historical examples should not be deleted until the expected compatibility requirements are clarified.
- `cloc` and tools designed for scanning the history for secrets were not available. The Java line counts provided above are raw figures; analysis of the history is limited to the available commits.

## 8. Risks and Effort for Subsequent Phases

Effort is estimated qualitatively based on the scope and visible dependencies; this does not constitute a calendar-based estimate.

| Phase | Primary Risk | Level | Effort |
|---|---|---|---|
| 1 — Reproducible Build / CI | Incomplete multi-module build, Java 1.5/implicit settings, legacy/unversioned dependencies, Ant tasks that may mask errors, insecure signing configuration. | High | L |
| 2 — Stabilization | JAI/JMF/JavaFX, Java2D/LWJGL rendering, resources and threads; actual execution not yet verified. | High | L |
| 3 — Determinism | Clocks/randomness/threads, `float`/`double` usage, logic distributed across Fighter/algorithm/rendering components. | High | L |
| 4 — Inputs and Controllers | JInput is present, but XInput is missing; any new dependency requires prior validation regarding licensing, maintenance, and native code. | Medium | M |
| 5 — AI | Existing implementation lacks reproducible test coverage; multi-controller integration remains to be defined. | Medium | M |
| 6 — Multiplayer | Network code drafted; Java serialization received via socket; no bounded protocol or desynchronization tracking in place. | Critical | L |
| 7 — Tools and packaging | Tools related to JXA/Applet/Swing and legacy dependencies; multi-OS launchers and examples requiring validation. | Medium | M |
| 8 — Mobile study | Heavy reliance on AWT/Swing/LWJGL; potential for cross-platform core decoupling. | High | L |

### Proposed Priority

1. Establish an end-to-end build process that can be verified without executing the embedded signature; clarify dependencies/versions and ensure Ant fails on any Java-related error.
2. Fix the compilation foundation and test Java2D rendering before implementing functional changes.
3. Map out time, randomness, and thread-related access points within the simulation; do not undertake invasive numerical conversion without prior agreement.
4. Secure network communication and define bounded packet tests before attempting any rollback-related work.
5. Proceed with inputs, AI, packaging, and mobile support, following the sequence outlined in `docs/COPILOT-PHASES.md`.
