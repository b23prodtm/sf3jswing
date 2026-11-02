# Changelog

## Unreleased

- Port the game module to the 4.6.8 framework API (`RenderingSceneGL`, viewport
  option names, `FileHelper` store directory, instance gamepad handler) so it
  compiles on JDK 17.
- CI: fetch `sbbi-upnplib` 1.0.4 (Apache-1.1, Maven Central) so the game module
  compiles its UPnP port-mapping classes.

- Remove the JAI-specific TIFF metadata and JDIC tray integrations while retaining
  generic ImageIO handling and the standard AWT system tray.
- Use standard ImageIO file streams in ImageMultiConverter instead of removed
  JAI ImageIO stream implementations.
- Build ImageMultiConverter's unsigned Linux amd64 jars successfully on JDK 17.
- Migrate framework MIME registration to Jakarta Activation and align its
  ThreadWorks calls with the available kernel API (which has no timeout).
- Resolve historical LWJGL, JInput, JLayer, OpenJFX, and JAXB dependencies
  through checksum-pinned Ant targets.
- Replace legacy Facebook/Picasa actions with clipboard-assisted links to
  Facebook and Google Photos.
- Use the public JavaFX `Platform.runLater` API in the IMC Browser.
- Remove IMC Browser's eSellerate base class, product-key/demo checks, stale
  validation keys, native libraries, and packaging references; build successfully
  as an unsigned Linux amd64 jar on Java 17.
- Define 3072-bit RSA keys and SHA256withRSA certificate signatures in active
  Ant key-generation targets; document the incomplete GUI/camera smoke checks.
