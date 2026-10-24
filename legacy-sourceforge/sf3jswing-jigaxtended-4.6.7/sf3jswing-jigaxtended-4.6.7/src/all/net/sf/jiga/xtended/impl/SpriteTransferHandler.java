package net.sf.jiga.xtended.impl;

import java.awt.Dimension;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.Image;

import java.awt.datatransfer.DataFlavor;

import java.awt.datatransfer.Transferable;

import java.awt.image.BufferedImage;

import java.io.File;

import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.JComponent;

import javax.swing.TransferHandler;

/**
 * the SpriteTransferHandler class provides support for importing datas into new
 * Sprite instances from a {@link java.awt.dnd Drag'n'Drop} operation or the
 * clipboard or another external data importing operation.
 *
 * It uses the Sprite class supported {@link DataFlavor DataFlavor's}.
 *
 * @see TransferHandler
 *
 * @see Sprite#_newTransferHandler()
 */
public class SpriteTransferHandler extends TransferHandler {

        /**
         * this TransferHandler transfered Sprite's
         *
         * @see #getTransferedData()
         */
        Sprite[] transferedData = new Sprite[]{};

        /**
         * returns the last transfered Sprite's of this SpriteTransferHandler
         * instance
         *
         * @return the last transfered Sprite's of this SpriteTransferHandler instance
         */
        public Sprite[] getTransferedData() {

                return transferedData;

        }

        /**
         * imports new data from the specified Transferable instance. The
         * tranfered data is converted in Sprite instances (if a list of file is
         * provided, then the corresponding amount of Sprite's is stored in the
         * transfered data array)
         *
         * @param comp the JComponent instance that is exporting the
         * Transferable
         *
         * @param t the Transferable instance that is transfered
         *
         * @return true or false, whether the Transferable has been accepted and
         * imported or not, resp.
         *
         * @see #getTransferedData()
         *
         * @see Sprite#_dataFlavors
         *
         */
        @Override
        public boolean importData(JComponent comp, Transferable t) {

                DataFlavor[] tdfs = t.getTransferDataFlavors();

                for (DataFlavor df : Sprite._dataFlavors) {

                        for (DataFlavor tdf : tdfs) {

                                if (df.equals(tdf)) {

                                        try {

                                                Object data = t.getTransferData(tdf);

                                                if (data instanceof List) {

                                                        transferedData = new Sprite[((java.util.List<File>) data).size()];

                                                        int i = 0;

                                                        for (File f : (List<File>) data) {

                                                                BufferedImage image = ImageIO.read(f);

                                                                transferedData[i++] = new Sprite(image, "image/png", new Dimension(image.getWidth(), image.getHeight()));

                                                        }

                                                        return true;

                                                } else if (data instanceof String) {

                                                        BufferedImage image = ImageIO.read(new File((String) data));

                                                        transferedData = new Sprite[]{new Sprite(image, "image/png", new Dimension(image.getWidth(), image.getHeight()))};

                                                        return true;

                                                } else if (data instanceof Image) {

                                                        transferedData = new Sprite[]{new Sprite(SpriteIO.createBufferedImage(new Dimension(64, 64), Sprite.DEFAULT_TYPE), "image/png", new Dimension())};

                                                        transferedData[0].getMt().addImage((Image) data, 0);

                                                        transferedData[0].getMt().waitForID(0);
                                                        transferedData[0].getMt().removeImage((Image) data, 0);
                                                        transferedData[0].setSize(((Image) data).getWidth(transferedData[0].getObs()), ((Image) data).getHeight(transferedData[0].getObs()));

                                                        Graphics dataG = Sprite._createImageGraphics(transferedData[0].getImage(transferedData[0].getObs()));

                                                        Graphics2D g = Sprite.wrapRendering(dataG);

                                                        g.drawImage((Image) data, null, transferedData[0].getObs());

                                                        dataG.dispose();

                                                        return true;

                                                } else if (data instanceof Sprite) {

                                                        transferedData = new Sprite[]{(Sprite) data};

                                                        return true;

                                                } else {

                                                        return false;

                                                }

                                        } catch (Exception ex) {

                                                ex.printStackTrace();

                                        }

                                }

                        }

                }

                return false;

        }

}
