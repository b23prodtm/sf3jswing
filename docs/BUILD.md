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

The current framework, game, IMC browser, converter, and JXA test module
descriptors target Java 17. The JXA kernel descriptor also targets Java 17.
The project setup workflow installs Temurin 17.

The current framework and its consumers resolve
`org.eclipse.imagen:imagen-legacy-all:0.9.3` from Maven Central through
`build-imagen.xml`. The downloaded jar is placed under the module's
`lib/all/`, is verified against a pinned SHA-256 digest, and is not tracked in
Git. ImageN is distributed under the Apache License 2.0 and requires Java 17.
Its `lookup` operation is not registered in this ImageN release, so the image
browser uses Java's standard `LookupOp`; perspective warping and histogram
operations use ImageN.

This dependency resolution does not replace the other historical libraries
omitted from the source archives. A complete module build still requires those
libraries in the legacy `lib/` directories. Use `ant build-jar` for an unsigned
jar; the default `jar` target invokes the historical signing configuration.

The archived Jigaxtended 4.6.7 sources remain unchanged. Only the current
4.6.8 framework sources have been migrated.
