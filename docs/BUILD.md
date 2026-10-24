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
| LWJGL `v.2.9.3-jxa` release jars and OS native ZIPs | Framework, game, and IMC Browser | BSD-3-Clause |
| `net.java.jinput:jinput:2.0.10` and `natives-all` | Framework/game controllers | BSD |
| `javazoom:jlayer:1.0.1` | Historical MP3 decoder dependency | LGPL |
| `jakarta.activation:jakarta.activation-api:2.1.3` and `org.eclipse.angus:angus-activation:2.0.2` | Framework MIME registry | EDL-1.0 |
| OpenJFX `17.0.21` (`base`, `graphics`, `controls`, `swing`, `web`, `media`) | IMC Browser JavaFX bridge | GPL-2.0 with Classpath Exception |
| JAXB API `2.1`, `com.sun.xml.bind:jaxb-impl:2.1.9`, `com.sun.activation:javax.activation:1.2.0` | IMC Browser XML runtime | CDDL/GPL-2.0 with Classpath Exception |

The FMJ artifact contains the JMF-compatible classes used by the browser.
Actual camera capture and device-specific runtime behavior are not verified.
Jigaxtended retains generic ImageIO TIFF reader/writer support where an
ImageIO provider supplies it, but no longer applies the historical JAI-specific
TIFF metadata/EXIF adjustments. The legacy JDIC tray backend is removed; the
standard AWT `SystemTray` backend remains and falls back to an in-window notice
when the desktop tray is unavailable.
Framework model/image loading uses the kernel's blocking `ThreadWorks` API,
which does not expose a timeout.
The game now loads Connector/J 9.7.0 through `com.mysql.cj.jdbc.Driver`;
database connectivity to the historical remote endpoint has not been tested.
The game reads its SQL user/password from `SF3JSWING_SQL_USER_RW`,
`SF3JSWING_SQL_PASSWORD_RW`, `SF3JSWING_SQL_USER_RO`, and
`SF3JSWING_SQL_PASSWORD_RO` environment variables; no credentials are
hard-coded. The game package includes Connector/J and Protobuf in the platform
classpath.
These third-party licenses are distinct from the repository's
LGPL-3.0-or-later license; retain each upstream license and attribution when
redistributing the dependencies.

The LWJGL PR #23 source archive was tried at commit
`676ace2e11847d83df6169f59434a0364eece5d6`. Its Ant build hard-codes Java
1.5; the source target can be run on JDK 8 but is not usable as part of the
required JDK 17 build. The build therefore uses the requested `v.2.9.3-jxa`
release Java jars and downloads only the matching 64-bit native files from its
Linux, macOS, or Windows ZIP, with SHA-256 checks. No native binaries or
downloaded jars are tracked. macOS Apple Silicon and 32-bit platforms are not
supported by these 2015 LWJGL binaries.

The IMC Browser resolves only the JavaFX platform classifier matching the
current OS. JAXB 2.1.9 passes a Java 17 marshal/unmarshal round-trip when the
JVM is launched with
`-Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true`; without that
property its runtime attempts reflective access blocked by Java modules.

GData/Picasa, RestFB, and the eSellerate `es-sdk-jdk-binding` remain deferred as
requested. They are still referenced by archived IMC Browser sources, along
with the removed Nashorn scripting API. The complete IMC Browser build is
therefore not yet resolved; framework/game/browser builds also depend on the
correctly built kernel/framework artifacts. This does not complete phase 1.
The descriptors fail on compiler errors rather than reporting a misleading
success. Use `ant build-jar` for an unsigned jar; the default `jar` target
invokes the historical signing configuration.

The archived Jigaxtended 4.6.7 sources remain unchanged. Only the current
4.6.8 framework sources have been migrated.
