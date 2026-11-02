# Copilot Instructions — sf3jswing
5. **Deliverables**: Executable JAR (`java -jar`), portable ZIP with launchers (`.sh`, `.bat`, `.command`), and an installer including OpenJDK + Ant/Maven. No proprietary tools.
6. **No secrets in the repository**: Never include keystores, certificates, passwords, or tokens. JAR signing is optional and must use GitHub secrets. Do not commit new native binaries; retrieve them from releases or Maven instead.
7. **Out of scope (unless explicitly requested)**: Gradle, Kotlin, LWJGL 3, migrating from Swing to JavaFX, renaming packages, global reformatting (to avoid noise in diffs), and using Java versions higher than 8.

## Workflow

- One issue = one concise, readable PR (target: fewer than 40 files and 400 lines modified, excluding generated files). If it exceeds this, break it down.
- Before coding: Read `docs/AUDIT.md` and `docs/COPILOT-PHASES.md`. Do not invent new build commands; use those specified in the audit.
- Compile and test before opening a PR. In the PR description, include: what has changed, how to test it, potential risks, and any components that could not be tested (e.g., OpenGL rendering, physical controllers, or real-world network conditions).
- **Pause and ask a question within the PR** if: there are 3 consecutive CI failures, or if there are changes to the public API, file formats, licenses, or dependencies. Do not guess.
- Tests: Use JUnit 4, executable in headless mode (`-Djava.awt.headless=true`, using Java2D software rendering). No external network or GPU access. Tests must be deterministic: use a fixed seed and avoid long `sleep` calls.
- Style: Follow the conventions of the file being modified. Use English for commit messages (following the Conventional Commits standard). Javadoc and comments for new files must be in English. Keep `CHANGELOG.md` up to date.
- CI: Use GitHub Actions with minimal permissions (`contents: read` by default) and pin actions to specific major versions.
- Networking: Bound and validate all incoming packets (size, value ranges). Never deserialize Java objects (`ObjectInputStream`) from network data.
