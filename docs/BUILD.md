# Build with Java 17

Ant remains the reference build. The imported SourceForge projects are nested
under `legacy-sourceforge/`; run Ant from a module's project directory, not the
repository root.

```sh
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
cd legacy-sourceforge/sf3jswing-jigaxtended-4.6.8/sf3jswing-jigaxtended-4.6.8
ant compile
```

The current framework, game, IMC browser, converter, JXA test, and kernel
descriptors target Java 17. They declare the historical Windows-1252 source
encoding explicitly. The setup workflow installs Temurin 17.

The kernel resolves Apache Commons HttpClient 3.1, Commons Codec 1.3,
Commons IO 2.1, and Commons Logging 1.1.1 from Maven Central through
`build-commons.xml`. Each downloaded jar is checked against a pinned SHA-256
digest before compilation; the jars are placed under `lib/all/` and are not
tracked in Git. These dependencies use the Apache License 2.0.

The current framework and its consumers resolve
`org.eclipse.imagen:imagen-legacy-all:0.9.3` from Maven Central through
`build-imagen.xml`. The downloaded jar is placed under the module's
`lib/all/`, is verified against a pinned SHA-256 digest, and is not tracked in
Git. ImageN is distributed under the Apache License 2.0 and requires Java 17.
ImageN 0.9.3 is a pre-release milestone. Its `lookup` operation is not
registered in this release, so the image browser uses Java's standard
`LookupOp`; perspective warping and histogram operations use ImageN.

The approved framework, IMC browser, and game dependencies are resolved from
Maven Central by `build-approved-dependencies.xml`; each artifact is pinned
and SHA-256 checked before compilation:

| Artifact | Used by | License |
|---|---|---|
| `org.swinglabs:swingx:0.9.5` | Framework and IMC browser widgets | LGPL-2.1-or-later |
| `org.jitsi:fmj:1.0.2-jitsi` | IMC browser's JMF-compatible capture APIs | LGPL-2.1-or-later |
| `com.mysql:mysql-connector-j:9.7.0` | Game database connector | GPL-2.0 with Universal FOSS Exception 1.0 |
| `com.google.protobuf:protobuf-java:4.31.1` | Connector/J runtime dependency | BSD-3-Clause |

The FMJ artifact contains the JMF-compatible classes used by the browser.
Actual camera capture and device-specific runtime behavior are not verified.
Connector/J 9.7.0 retains the legacy `com.mysql.jdbc.Driver` class alias used by
the game; database connectivity to the historical remote endpoint has not been
tested. The game package includes Connector/J and Protobuf in the platform
classpath. These third-party licenses are distinct from the repository's
LGPL-3.0-or-later license; retain each upstream license and attribution when
redistributing the dependencies.

This does not resolve every historical dependency omitted from the source
archives. Java 17 Ant builds may still stop on LWJGL and kernel classes for
Jigaxtended, the compiled framework for the game, and JavaFX/OpenJFX, JAXB,
Google Data API, RestFB, and other APIs for the IMC browser. JAXB 2.1.9 is
published on Maven Central, but its Java 17 runtime compatibility is
unverified. The descriptors fail on compiler errors rather than reporting a
misleading success. Use `ant build-jar` for an unsigned jar; the default `jar`
target invokes the historical signing configuration.

The archived Jigaxtended 4.6.7 sources remain unchanged. Only the current
4.6.8 framework sources have been migrated.
