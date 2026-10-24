package net.sf.jiga.xtended.impl.game;

import java.awt.DisplayMode;
import java.awt.Rectangle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DisplayModeWrapper implements Comparable, Externalizable {

    private static final long serialVersionUID = 2323;
    private transient DisplayMode dm;
    private transient org.lwjgl.opengl.DisplayMode dmGL;

    public DisplayModeWrapper() {
        dm = null;
        dmGL = null;
    }

    public DisplayModeWrapper(DisplayMode mode) {
        dm = mode;
        dmGL = null;
    }

    public DisplayModeWrapper(org.lwjgl.opengl.DisplayMode mode) {
        dm = null;
        dmGL = mode;
    }

    public DisplayMode getDM() {
        return dm != null ? dm : new DisplayMode(getWidth(), getHeight(), getBitDepth(), getRefreshRate());
    }

    /**
     * caution : may not return a fullscreen capable displaymode, the
     * refreshrate may not be reflected.
     */
    public org.lwjgl.opengl.DisplayMode getDmGL() {
        return dmGL != null ? dmGL : new org.lwjgl.opengl.DisplayMode(getWidth(), getHeight());
    }

    @Override
    public String toString() {
        String str = getWidth() + "x" + getHeight() + "@ " + getRefreshRate() + " Hz " + getBitDepth() + " bits";
        return str;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DisplayModeWrapper) {
            return hashCode() == object.hashCode();
        }
        return false;
    }
    static long hashRoot = System.nanoTime();

    @Override
    public int hashCode() {
        int hash = (int) hashRoot;
        if (dm instanceof DisplayMode) {
            hash = hash + this.dm.hashCode();
        } else if (dmGL instanceof org.lwjgl.opengl.DisplayMode) {
            hash = hash + this.dmGL.hashCode();
        }
        return hash;
    }

    @Override
    public int compareTo(Object arg0) {
        if (arg0 instanceof DisplayModeWrapper) {
            DisplayModeWrapper dmw = ((DisplayModeWrapper) arg0);
            Rectangle resArg = new Rectangle(dmw.getWidth(), dmw.getHeight()), res = new Rectangle(getWidth(), getHeight());
            int returnVal = res.equals(resArg) ? 0 : res.equals(resArg.createIntersection(res)) ? -1 : 1;
            switch (returnVal) {
                case 0:
                    returnVal = ((Integer) getBitDepth()).compareTo((Integer) dmw.getBitDepth());
                    break;
                default:
                    return returnVal;
            }
            switch (returnVal) {
                case 0:
                    returnVal = ((Integer) getRefreshRate()).compareTo((Integer) dmw.getRefreshRate());
                    break;
                default:
                    return returnVal;
            }
            return returnVal;
        } else {
            return -1;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(getWidth());
        out.writeInt(getHeight());
        out.writeInt(getBitDepth());
        out.writeInt(getRefreshRate());
        out.writeInt((dm != null) ? 1 : (dmGL != null) ? 2 : 0);
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int width, height, bitDepth, refreshRate;
        width = in.readInt();
        height = in.readInt();
        bitDepth = in.readInt();
        refreshRate = in.readInt();
        switch (in.readInt()) {
            case 1:
                dm = new DisplayMode(width, height, bitDepth, refreshRate);
                break;
            case 2:
                dmGL = new org.lwjgl.opengl.DisplayMode(width, height);
                break;
            default:
                break;
        }
    }

    public int getWidth() {
        if (dm instanceof DisplayMode) {
            return dm.getWidth();
        } else if (dmGL instanceof org.lwjgl.opengl.DisplayMode) {
            return dmGL.getWidth();
        } else {
            return 0;
        }
    }

    public int getHeight() {
        if (dm instanceof DisplayMode) {
            return dm.getHeight();
        } else if (dmGL instanceof org.lwjgl.opengl.DisplayMode) {
            return dmGL.getHeight();
        } else {
            return 0;
        }
    }

    public int getRefreshRate() {
        if (dm instanceof DisplayMode) {
            return dm.getRefreshRate();
        } else if (dmGL instanceof org.lwjgl.opengl.DisplayMode) {
            return dmGL.getFrequency();
        } else {
            return 0;
        }
    }

    public int getBitDepth() {
        if (dm instanceof DisplayMode) {
            return dm.getBitDepth();
        } else if (dmGL instanceof org.lwjgl.opengl.DisplayMode) {
            return dmGL.getBitsPerPixel();
        } else {
            return 0;
        }
    }
}
