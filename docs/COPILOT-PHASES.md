# Finalization Plan for sf3jswing — Phases for Copilot

---

## Phase 7 — Tools, Packaging, and Applet

**Objective:** Provide tools that can be used without a browser, along with installable deliverables.

**Tasks**
- Character/level editors and browsers (`imcbrowser`, `animsbrowser`) launched as desktop windows.
- Remove the Applet from the default build (move to an optional `applet` profile; retain source code).
- Create launch scripts (`.sh`, `.bat`, `.command`), a portable ZIP archive, and a `samples/` directory (containing an example character and level).
- Create `docs/TOOLS.md`: instructions on how to create a character, build a level, and test them within the game.
- Optional: Generate `.deb`/`.rpm`/`.msi` packages using `jpackage` (requires JDK 17 or higher; for build-time use only).

**Acceptance Criteria:** Starting from a fresh ZIP archive, the user should be able to create and launch an example character by following the steps in `docs/TOOLS.md`.

---

## Phase 8 — Mobile Feasibility Study (No Rewriting)

**Objective:** Assess the effort required to port the project to Android and iOS while remaining in Java.

**Tasks**
- Refactor the `engine-core` module into pure Java (no `java.awt`, `javax.swing`, or `org.lwjgl` imports); verify via CI.
- Develop a libGDX prototype (desktop + Android) that displays an AI-vs-AI match.
- iOS: Conduct a feasibility study only (covering MobiVM requirements, estimated effort, and limitations). No implementation.
- Create `docs/MOBILE.md`: Identify AWT/LWJGL dependencies that need to be decoupled, categorized by the level of effort required, with time estimates in days.

**Acceptance Criteria:** A functional prototype that runs on both desktop and an Android emulator; a completed `docs/MOBILE.md` document.
