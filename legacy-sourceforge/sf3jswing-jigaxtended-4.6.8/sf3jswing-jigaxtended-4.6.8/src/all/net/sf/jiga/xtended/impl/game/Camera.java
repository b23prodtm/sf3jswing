/*

 * To change this template, choose Tools | Templates

 * and open the template in the editor.

 */

package net.sf.jiga.xtended.impl.game;



import java.awt.Dimension;

import java.awt.Point;

import java.awt.Polygon;

import java.awt.Rectangle;

import java.awt.Shape;

import java.awt.geom.Rectangle2D;



/**

 * Camera class is intended to format the backgrounds shapes for using them with a specific PerspectiveTransform matrix.

 * @author www.b23prodtm.info

 */

public class Camera {

    /** this Camera bounds*/

    Rectangle bounds;

    /** this Camera center position */

    Point position;

    /** this Camera floor height from the bottom-side in px*/

    int floor;

    /** this Camera roof height from the top-side in px*/

    int roof;

    /** this Camera roof background Shape*/

    Polygon roofShape;

    /** this Camera roof background Shape when the Camera is centered*/

    Polygon centeredRoofShape;

    /** this Camera floor background Shape*/

    Polygon floorShape;

    /** this Camera floor background Shape when the Camera is centered*/

    Polygon centeredFloorShape;

    /** this Camera background Shape between the floor and the roof*/

    Rectangle backgroundRect;

    /** the actual zoom value */

    double zoom = 1.0;



    /** creates a Camera instance

     @param bounds the bounds for this Camera to init with; it will be centered at the center of this bounds

     @param floor the floor height from the bottom-side in px

     @param roof the roof height from the top-side in px*/

    public Camera(Rectangle2D bounds, int floor, int roof) {

        this.bounds = bounds.getBounds();

        this.floor = floor;

        this.roof = roof;

        centerCamera(bounds);

    }



    /** creates a Camera instance, the camera will center at the specified position

     @param position the center position for this Camera to init at

     @param size the size of this Camera, the Camera bounds will build with this size value and the specified center position

     @param floor the floor height from the bottom-side in px

     @param roof the roof height from the top-side in px*/

    public Camera(Point position, Dimension size, int floor, int roof) {

        bounds = new Rectangle(size);

        this.position = position;

        this.floor = floor;

        this.roof = roof;

        centerCamera(position);

    }    



    /** builds the roof Shape

     @param roof the roof height from the top-side in px

     @param centered whether this Camera is already centered or not*/

    private void buildRoof(int roof, boolean centered) {

        int xB = (int) ((float) (bounds.getMaxX() - position.x) / (float) (-(roof - position.y + bounds.getY())) * (float) (position.y - bounds.getY()) + (float) position.x);

        if (!position.equals(new Point((int) bounds.getMaxX(), (int) (bounds.getY() + roof)))) {

            double width;

            if(centered)

                width = bounds.getWidth() * position.distance((double)xB, bounds.getY()) / position.distance(bounds.getMaxX(), bounds.getY() + roof);

            else

                width = centeredRoofShape.getBounds2D().getWidth();

            roofShape = new Polygon(

                    new int[]{

                (int) (xB - width),

                xB,

                (int) bounds.getMaxX(),

                (int) bounds.getX()

            },

                    new int[]{

                (int) bounds.getY(),

                (int) bounds.getY(),

                (int) (bounds.getY() + roof),

                (int) (bounds.getY() + roof)

            },

                    4);

        } else {

            System.err.println(toString() + " cannot build the roof shape.");

        }

    }



    /** builds the floor Shape for this Camera

     @param floor the floor height from the bootom-side in px

     @param centered whether this Camera is already centered or not*/

    private void buildFloor(int floor, boolean centered) {

        int xB = (int) ((float) (bounds.getMaxY() - position.y) / ((float) (bounds.getMaxY() - floor - position.y) / (float) (bounds.getMaxX() - position.x)) + (float) position.x);

        if (!position.equals(new Point((int) bounds.getMaxX(), (int) (bounds.getMaxY() - floor)))) {            

            double width;

            if(centered)

                width = bounds.getWidth() * position.distance((double)xB, bounds.getMaxY()) / position.distance(bounds.getMaxX(), bounds.getMaxY() - floor);

            else

                width = centeredFloorShape.getBounds2D().getWidth();

            floorShape = new Polygon(

                    new int[]{

                (int) bounds.getX(),

                (int) bounds.getMaxX(),

                xB,

                (int) (xB - width)

            },

                    new int[]{

                (int) (bounds.getMaxY() - floor),

                (int) (bounds.getMaxY() - floor),

                (int) bounds.getMaxY(),

                (int) bounds.getMaxY()

            },

                    4);

        } else {

            System.err.println(toString() + " cannot build the floor shape.");

        }

    }



    /** builds the background Shape for this Camera

     @param floor the floor height from the bottom-side in px

     @param roof the roof height from the top-side in px

     @param centered whether this Camera is already centered or not*/

    private void buildBackground(int floor, int roof, boolean centered) {

        buildRoof(roof, centered);

        buildFloor(floor, centered);

        if (roofShape instanceof Shape && floorShape instanceof Shape) {

            backgroundRect = new Rectangle(

                    (int) roofShape.xpoints[3],

                    (int) roofShape.ypoints[3],

                    (int) (floorShape.xpoints[1] - floorShape.xpoints[0]),

                    (int) (floorShape.getBounds2D().getY() - roofShape.getBounds2D().getMaxY()));

        } else {

            System.err.println(toString() + " cannot build the background shape.");

        }

    }



    /** computes the specified zoom value to the corresponding bounds for this Camera instance 

     @param zoom the zoom value to use, 1.0 is no-zoom*/

    private void zoom(double zoom) {

        this.zoom = zoom;

        zoom = -(int) (2.0 * zoom);

        bounds.grow((int) Math.abs((double) bounds.getWidth() * zoom), (int) Math.abs((double) bounds.getHeight() * zoom));



    }



    /** centers this Camera 

     @param bounds centers this Camera with the specified bounds*/

    private void centerCamera(Rectangle2D bounds) {

        this.bounds = bounds.getBounds();

        position = new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());

        buildBackground(floor, roof, true);

        centeredRoofShape = roofShape;

        centeredFloorShape = floorShape;

    }



    /** centers this Camera 

     @param position centers this Camera with the specified center position*/

    private void centerCamera(Point position) {

        this.position = position;

        bounds.setLocation((int) (bounds.getX() + position.x - bounds.getCenterX()), (int) (bounds.getY() + position.y - bounds.getCenterY()));

        buildBackground(floor, roof, true);

        centeredRoofShape = roofShape;

        centeredFloorShape = floorShape;

    }



    /** returns the current bounds of this Camera instance

     @return the current bounds of this Camera instance*/

    public Rectangle2D getBounds2D() {

        return bounds;

    }



    /** returns the current roof Shape of this Camera instance

     @return the current roof Shape of this Camera instance */

    public Polygon getRoofShape() {

        return roofShape;

    }



    /** returns the current floor Shape of this Camera instance

     @return the current floor Shape of this Camera instance*/

    public Polygon getFloorShape() {

        return floorShape;

    }



    /** returns the current center background Shape of this Camera instance (a rectangle)

     @return the current center background Shape of this Camera instance (a rectangle)*/

    public Polygon getBackgroundRect() {

        Polygon shape = new Polygon(

                new int[]{

                backgroundRect.x,

                backgroundRect.x + backgroundRect.width,

                backgroundRect.x + backgroundRect.width,

                backgroundRect.x

        }, 

                new int[]{

            backgroundRect.y,

            backgroundRect.y,

            backgroundRect.y + backgroundRect.height,

            backgroundRect.y + backgroundRect.height

        },

                4);

        return shape;

    }



    /** returns the current center position of this Camera instance

     @return the current center position of this Camera instance*/

    public Point getPosition() {

        return position;

    }



    /** returns the current roof height from the top-side

     @return the current roof height from the top-side in px*/

    public int getRoof() {

        return roof;

    }



    /** returns the current floor height from the bottom-side

     @return the current floor height from the bottom-side in px*/

    public int getFloor() {

        return floor;

    }



    /** sets up the zoom value and computes the corresponding bounds for this Camera instance

     ?param zoom the zoom value to compute the corresponding bounds for this Camera instance

     @see #zoom(double)*/

    public void setZoom(double zoom) {

        zoom(zoom);

    }



    /** returns the current zoom value for this Camera instance

     @return the current zoom value for this Camera instance*/

    public double getZoom() {

        return zoom;

    }



    /** moves this Camera instance and computes the corresponding Shape's for the backgrounds, without moving the center position.

     ?@param h the horizontal move on the X-axis

     @param v the vertical move on the Y-axis

     @see #getRoofShape()

     @see #getFloorShape()

     @see #getBackgroundRect()

     @see Rectangle#setLocation(int, int)*/

    public void move(int h, int v) {

        bounds.setLocation((int) (bounds.getX() + h), (int) (bounds.getY() + v));

        buildBackground(floor, roof, false);

    }



    /** returns this Camera converted in a String instance

     @return this Camera instance converted in a comprehensive String instance*/

    @Override
    public String toString() {

        return getClass().getName() + " bounded w/ : " + bounds + " centered at : " + position + " w/ roof : " + roof + " and floor : " + floor + " w/ zoom : " + zoom;

    }

}

