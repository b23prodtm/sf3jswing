# Changelog

## Unreleased

- Remove the JAI-specific TIFF metadata and JDIC tray integrations while retaining
  generic ImageIO handling and the standard AWT system tray.
- Migrate framework MIME registration to Jakarta Activation and align its
  ThreadWorks calls with the available kernel API (which has no timeout).
- Resolve historical LWJGL, JInput, JLayer, OpenJFX, and JAXB dependencies
  through checksum-pinned Ant targets.
- Keep social publishing and eSellerate product-key integrations deferred.
- Use the public JavaFX `Platform.runLater` API in the IMC Browser.
