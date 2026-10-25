# Changelog

## Unreleased

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
