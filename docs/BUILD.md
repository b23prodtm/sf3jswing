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

This dependency resolution does not replace the other historical libraries
omitted from the source archives. Java 17 Ant compile attempts may still stop
on missing dependencies: kernel and
LWJGL classes for Jigaxtended, the compiled framework for the game, and the
framework/JMF/JavaFX and other historical APIs for the IMC browser. FMJ is
available on Maven Central but is LGPL-2.1-or-later; SwingX 0.9.5 is available
on Maven Central but its license and Java 17 runtime behavior need review
before adoption. JAXB 2.1.9 is also published there, but its Java 17 runtime
compatibility is unverified. The
descriptors now fail the build on compiler errors rather than reporting a
misleading success. Use `ant build-jar` for an unsigned jar; the default `jar`
target invokes the historical signing configuration.

The archived Jigaxtended 4.6.7 sources remain unchanged. Only the current
4.6.8 framework sources have been migrated.
