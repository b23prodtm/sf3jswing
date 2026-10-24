/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Rectangle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.DoubleBuffer;
import net.sf.jiga.xtended.impl.system.BufferIO;

/**
 *
 * @author www.b23prodtm.info
 */
public class ROI implements Externalizable{

    /**
     * raster area
     */
    public Rectangle ROI;
    /**
     * raster z pos
     */
    public double winZ;

    public ROI() {
        this(new Rectangle(), 0);
    }

    public ROI(Rectangle ROI, double winZ) {
        this.ROI = ROI;
        this.winZ = winZ;
    }

    public ROI(DoubleBuffer data) {
        this(new Rectangle(), data.get(4));
        ROI.setFrame(data.get(0), data.get(1), data.get(2), data.get(3));
    }

    /**
     * @return a DoubleBuffer(x,y,width,height,z)
     */
    public DoubleBuffer toMemScratchDataStore() {
        return BufferIO._wrapd(new double[]{ROI.getX(), ROI.getY(), ROI.getWidth(), ROI.getHeight(), winZ});
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(ROI);
        out.writeDouble(winZ);
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ROI = (Rectangle) in.readObject();
        winZ = in.readDouble();
    }
}
