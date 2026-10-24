/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 * implements a full Drag and Drop context, for using with an Ant class. The
 * DnDHandler extends the TransferHandler to implement import/export to
 * {@linkplain #addDropTarget(JComponent) components it has been added}.
 * Imported data are allowed by DnDHandler
 * {@linkplain #setImportedDataFlavors(DataFlavor[])} whereas Exported data is
 * allowed by the Ant {@linkplain Ant#getTransferDataFlavors()}.
 *
 * @author www.b23prodtm.info
 */
public final class DnDHandler extends TransferHandler implements ClipboardOwner {

    Ant ant;

    public DnDHandler(Ant ant) {
        super();
        this.ant = ant;
        setImportedDataFlavors(ant.getTransferDataFlavors());
    }
    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o != null ? o.hashCode() == hashCode() : false;
    }

    @Override
    public void exportToClipboard(JComponent pcomp, Clipboard pclip, int action) throws IllegalStateException {
        final Clipboard clip = pclip;
        final JComponent comp = pcomp;
        TransferHandler th = comp.getTransferHandler();
        if (th != null) {
            if (th.hashCode() == hashCode() && ant instanceof Transferable) {
                switch (action) {
                    case COPY:
                        clip.setContents((Transferable) ant, DnDHandler.this);
                        break;
                    default:
                        break;
                }
                return;
            }
        }
    }

    @Override
    public boolean importData(JComponent comp, final Transferable t) {
        boolean ret = false;
        /**/
        if (ant instanceof Transferable) {
            DataFlavor df = null;
            for (DataFlavor tdf : t.getTransferDataFlavors()) {
                if (ant.isDataFlavorSupported(tdf)) {
                    df = tdf;
                    break;
                }
            }
            if (df == null) {
                ret = false;
                System.err.println(JXAenvUtils.log("unsupported object : " + Arrays.toString(t.getTransferDataFlavors()), JXAenvUtils.LVL.SYS_NOT));
            } else {
                long wait = UIMessage.displayWaiting("dropping...", comp);
                try {
                    ant.open(t.getTransferData(df));
                    ret = true;
                } catch (UnsupportedFlavorException ex) {
                        Logger.getLogger(JXAenvUtils.LOGGER_NAME).log(Level.ALL, null, ex);
                        ret = false;
                } catch (IOException ex) {
                        Logger.getLogger(JXAenvUtils.LOGGER_NAME).log(Level.ALL, null, ex);
                    ret = false;
                } finally {
                    UIMessage.kill(wait);
                }
            }
        }
        return ret;
    }

    /**
     * adds a DropTarget JComponent for using Drag'n'Drop with this application.
     * Actually the specified JComponent wil become reactive when dragging some
     * {@linkplain #getImportedDataFlavors()} compatible DataFlavor's.
     *
     * @param comp a JComponent that will be enabled for Drag'n'Drop and the
     * current DataFlavor's
     */
    public void addDropTarget(final JComponent comp) {
        addDropTarget(comp, new DropTargetAdapter() {

            @Override
            public void dragExit(DropTargetEvent dte) {
                super.dragExit(dte);
                dte.getDropTargetContext().getComponent().setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                int dragAct = dtde.getDropAction();
                dtde.acceptDrag(dragAct);
                System.err.println("Drag ev : " + Arrays.toString(dtde.getTransferable().getTransferDataFlavors()));
                dtde.getDropTargetContext().getComponent().setCursor(dragAct == DnDConstants.ACTION_LINK ? DragSource.DefaultLinkDrop : dragAct == DnDConstants.ACTION_COPY ? DragSource.DefaultCopyDrop : dragAct == DnDConstants.ACTION_REFERENCE ? DragSource.DefaultLinkDrop : DragSource.DefaultMoveDrop);
            }

            public void drop(DropTargetDropEvent dtde) {
                System.err.println("Drop ev : " + dtde);
                dtde.acceptDrop(dtde.getDropAction());
                dtde.dropComplete(importData(comp, dtde.getTransferable()));
                dtde.getDropTargetContext().getComponent().setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    /**
     * @param dtadp using a custom adapter, for custom action on dragndrop
     * events
     *
     */
    public void addDropTarget(final JComponent comp, DropTargetAdapter dtadp) {
        String dataFlavors = "";
        String sep = "";
        for (DataFlavor df : getImportedDataFlavors()) {
            if (!df.equals(DataFlavor.stringFlavor) && !df.equals(DataFlavor.javaFileListFlavor)) {
                dataFlavors += sep + df.getHumanPresentableName();
                sep = ", ";
            }
        }
        comp.setToolTipText("Please drop here " + dataFlavors + " !");
        comp.setDropTarget(new DropTarget(comp, dtadp));
    }

    /**
     * changes the dataflavors to allow import
     */
    public void setImportedDataFlavors(DataFlavor[] dfs) {
        this.dfs = dfs;
    }
    DataFlavor[] dfs = new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};

    /**
     * allowd dataflavors to import
     *
     * @see #setImportedDataFlavors(DataFlavor[])
     */
    public DataFlavor[] getImportedDataFlavors() {
        return dfs;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
