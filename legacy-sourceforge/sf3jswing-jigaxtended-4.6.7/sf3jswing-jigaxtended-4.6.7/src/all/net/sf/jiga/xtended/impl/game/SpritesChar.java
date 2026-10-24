package net.sf.jiga.xtended.impl.game;

import java.awt.Dimension;

import java.io.File;

import java.io.IOException;


import java.io.Externalizable;
import java.io.ObjectInput;

import java.io.ObjectOutput;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jiga.xtended.impl.game.physics.Player;

/**

 * this abstract clas is the super Class for all Model's and InteractiveModel's sub-classes that need its specific mapping for implementing a good game mapping strategy.

 * Model refers to those mapping to store the Animation's file names, properties and other attributes. * 

 */
public abstract class SpritesChar implements Comparable, Externalizable {

    /** serial version UID */
    private static final long serialVersionUID = 2323;
    /** character's id */
    public String id = "";
    /** height between bottom of pics to bottom of the sprites in pixels */
    public int floorDiff = 0;
    /** width of the pics */
    public int spriteWidth = 0;
    /** height of the pics*/
    public int spriteHeight = 0;
    /** average internal size of the sprites in the pics */
    public Dimension spriteSize = new Dimension(0, 0);
    /** the frames indexes [0[start][end]][1[start][end]]..., part of the file names*/
    public int[][] frames = new int[][]{{0, 0}};
    /** the Animation's identifiers that may be eventually reversed playing */
    public String[] reversedAnims = new String[]{""};
    /** sound effects file names */
    public String[] sfx = new String[]{""};

    /** @return a formal default name that may be used to map the specified animation
    @default "anim-" + index*/
    public static String _getDefaultAnimName(int index) {
        return "anim-" + index;
    }
    /** Model's default Animation's identifier which should be
     * the begginning name for all animations as it would loop with it
    @default {@linkplain #_getDefaultAnimName(int)}*/
    public String animsID_default = _getDefaultAnimName(0);
    /** the Model's Animation's identifiers
     */
    public String[] animsID = new String[]{};
    /** the Model's Animation's prefix for the file names
    
    @default ""*/
    public String prefix = "";
    /** the Model's Animation's suffix for the file names
    
    @default ""*/
    public String suffix = "";
    /** the InteractiveModel's map , some demonstrative InteractiveModel instances are available*/
    public static Map<String, InteractiveModel> characters = Collections.synchronizedMap(new HashMap<String, InteractiveModel>());
    /** the Model's map , some demonstrative InteractiveModel instances are available*/
    public static Map<String, Model> models = Collections.synchronizedMap(new HashMap<String, Model>());
    /** the LOW resolution id */
    public final static int LOW = 0;
    /** the MID resolution id */
    public final static int MID = 1;
    /** the HIGH resolution id */
    public final static int HIGH = 2;
    /** the available resolutions */
    public final static int[] RES = new int[]{LOW, MID, HIGH};
    /** the LOW resolution rendering factor */
    private final static double LOW_FACTOR = 0.33;
    /** the MID resolution rendering factor */
    private final static double MID_FACTOR = 0.66;
    /** the HIGH resolution rendering factor */
    private final static double HIGH_FACTOR = 1.0;
    /** the xpng image file format */
    private final static String[] XPNG = new String[]{"image/x-png", ".png"};
    /** the png image file format */
    private final static String[] PNG = new String[]{"image/png", ".png"};
    /** the gif image file format */
    private final static String[] GIF = new String[]{"image/gif", ".gif"};
    /** the bmp image file format*/
    private final static String[] BMP = new String[]{"image/bmp", ".bmp"};
    /** the jpeg image file format */
    private final static String[] JPEG = new String[]{"image/jpeg", ".jpg"};

    /***/
    SpritesChar() {
        super();
    }
    /** the resolution names map 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final static Map<Integer, String> RES_NAMES_map = Collections.synchronizedMap(new HashMap<Integer, String>());
    /** the resolution factors map 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final static Map<Integer, Double> RES_FACTORS_map = Collections.synchronizedMap(new HashMap<Integer, Double>());
    /** the resolution directory paths map
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final static Map<Integer, String> RES_PATHS_map = Collections.synchronizedMap(new HashMap<Integer, String>());
    /** the resolution file extensions map 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final static Map<Integer, String> RES_EXT_map = Collections.synchronizedMap(new HashMap<Integer, String>());
    /** the resolution mime-types map 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final static Map<Integer, String> RES_MIMETYPES_map = Collections.synchronizedMap(new HashMap<Integer, String>());

    static {
        RES_PATHS_map.put(LOW, "low");

        RES_PATHS_map.put(MID, "mid");

        RES_PATHS_map.put(HIGH, "");
        RES_NAMES_map.put(LOW, "low resolution");

        RES_NAMES_map.put(MID, "middle resolution");

        RES_NAMES_map.put(HIGH, "high resolution");

        RES_FACTORS_map.put(LOW, LOW_FACTOR);

        RES_FACTORS_map.put(MID, MID_FACTOR);

        RES_FACTORS_map.put(HIGH, HIGH_FACTOR);        

        RES_EXT_map.put(LOW, XPNG[1]);

        RES_EXT_map.put(MID, XPNG[1]);

        RES_EXT_map.put(HIGH, PNG[1]);

        RES_MIMETYPES_map.put(LOW, XPNG[0]);

        RES_MIMETYPES_map.put(MID, XPNG[0]);

        RES_MIMETYPES_map.put(HIGH, PNG[0]);

    }
    /** file resolution to sprites images
    
     * @see #RES_EXT_map
    
     * @see #RES_MIMETYPES_map
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public int res;
    /** the default extension 
    
    @default PNG*/
    public static String DEFAULT_EXT = PNG[0];
    /** the KeyEvent's keyCode map 
    
    @see Player#_keyCodeMap*/
    public final static Map<Player.key, Integer> keyCodeMap = Player._keyCodeMap;

    /** returns the directory path to Sprite files for the specified resolution
    
     * @param res LOW, MID or HIGH
    
     * @return String path to the Sprite files for the specified resolution
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH
    
    @see #RES_PATHS_map*/
    public final String getPath(boolean rsrcMode, int res) {
        String resPath = null;
        switch (res) {
            case LOW:
                resPath = RES_PATHS_map.get(LOW);
                break;
            case HIGH:
                resPath = RES_PATHS_map.get(HIGH);
                break;
            case MID:
                resPath = RES_PATHS_map.get(MID);
                break;
            default:
                break;
        }

        if (resPath != null) {
            return id + (!resPath.equals("") ? (rsrcMode ? "/" : File.separator) + resPath : "");
        } else {
            return id;
        }
    }

    /** @deprecated instance a new InteractiveModel or Model instead !
    
    @see InteractiveModel
    
    @see Model*/
    public static Custom getCustomSpritesChar(int res) {

        return new Custom(res);

    }

    /** @deprecated instance a new InteractiveModel or Model instead !
    
    @see InteractiveModel
    
    @see Model */
    public static Custom getCustomSpritesChar(String name, int res, int floorDiff, Dimension spriteDim, Dimension spriteSize, int[][] framesNum, String[] reversedAnims, String[] sfxRsrcs) {

        return new Custom(name, res, floorDiff, spriteDim, spriteSize, framesNum, reversedAnims, sfxRsrcs);

    }

    /** returns the id of the instance 
    
    @return the id of the instance */
    @Override
    public String toString() {

        return id;

    }

    /** compares 2 instances 
    
    @return 0 for equality, 1 or -1 for inequality */
    @Override
    public int compareTo(Object o) {

        if (o instanceof SpritesChar) {

            return id.compareTo(((SpritesChar) o).id);

        }

        return 1;

    }
    /***/
    long hash = System.nanoTime();

    /***/
    @Override
    public int hashCode() {
        return (int) hash;
    }

    /***/
    @Override
    public boolean equals(Object o) {

        return o == null ? false : o.hashCode() == hashCode();
    }

    /** returns an index for the specified Animation's identifier 
    
    @return 0 by default*/
    public int anim(String name) {

        return 0;

    }

    /** returns an index for the specified Animation's identifier corresponding to the sound file name index in the sfxRsrcs map 
    
    @return 0 by default */
    public int sound(String name) {

        return 0;

    }

    /** sets up the resolution to apply for this instance      
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH*/
    public final void setResolution(int res) {

        assert RES_NAMES_map.containsKey(res) : "the specified resolution is unknown, please set up RES_NAMES_map !";

        this.res = res;

    }

    /** returns the resolution applied to this instance 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH
    
    @see #setResolution(int)*/
    public final int getResolution() {

        return res;

    }

    /** returns the associated floor difference in the Sprite's for the specified resolution id 
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH
    
    @return the floor difference in pixels for the specified resolution 
    
    @see #RES_FACTORS_map*/
    public int getFloorDiff(int spritesRes) {

        return (int) ((float) floorDiff * RES_FACTORS_map.get(spritesRes).floatValue());
    }

    /***/
    public Dimension getInsetSize(int spritesRes) {

        return new Dimension((int) ((float) spriteSize.width * RES_FACTORS_map.get(spritesRes).floatValue()), (int) ((float) spriteSize.height * RES_FACTORS_map.get(spritesRes).floatValue()));
    }

    /** returns the Sprite's width for the specified resolution
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH
    
    @return the width for the specified resolution
    
    @see #RES_FACTORS_map*/
    public int getWidth(int spritesRes) {

        return (int) ((float) spriteWidth * RES_FACTORS_map.get(spritesRes).floatValue());
    }

    /** returns the Sprite's height for the specified resolution
    
    @see #LOW
    
    @see #MID
    
    @see #HIGH
    
    @return the height for the specified resolution
    
    @see #RES_FACTORS_map*/
    public int getHeight(int spritesRes) {

        return (int) ((float) spriteHeight * RES_FACTORS_map.get(spritesRes).floatValue());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        out.writeObject(animsID);
        out.writeUTF(animsID_default);
        out.writeInt(floorDiff);
        out.writeObject(frames);
        out.writeLong(hash);
        out.writeUTF(id);
        out.writeUTF(prefix);
        out.writeInt(res);
        out.writeObject(reversedAnims);
        out.writeObject(sfx);
        out.writeInt(spriteHeight);
        out.writeInt(spriteSize.width);
        out.writeInt(spriteSize.height);
        out.writeInt(spriteWidth);
        out.writeUTF(suffix);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        animsID = (String[]) in.readObject();
        animsID_default = in.readUTF();
        floorDiff = in.readInt();
        frames = (int[][]) in.readObject();
        hash = in.readLong();
        id = in.readUTF();
        prefix = in.readUTF();
        res = in.readInt();
        reversedAnims = (String[]) in.readObject();
        sfx = (String[]) in.readObject();
        spriteHeight = in.readInt();
        spriteSize = new Dimension(in.readInt(), in.readInt());
        spriteWidth = in.readInt();
        suffix = in.readUTF();
        Thread.currentThread().setPriority(currentPty);
    }
}
